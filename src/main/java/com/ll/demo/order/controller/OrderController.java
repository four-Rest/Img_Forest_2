package com.ll.demo.order.controller;


import com.ll.demo.article.entity.Article;
import com.ll.demo.article.service.ArticleService;
import com.ll.demo.global.response.GlobalResponse;
import com.ll.demo.member.entity.Member;
import com.ll.demo.member.service.MemberService;
import com.ll.demo.order.entity.Order;
import com.ll.demo.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final MemberService memberService;
    private final ArticleService articleService;

    @Value("${custom.tossPayments.widget.secretKey")
    private String tossApiKey;

    // 주문 상세
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public GlobalResponse showDetail(@PathVariable("id") long id, Principal principal) {
       Order order = orderService.findById(id).orElse(null);

       if (order == null) {
           throw new IllegalArgumentException("주문을 찾을 수 없습니다");
       }
       Member member = memberService.findByUsername(principal.getName());

       long restCash = member.getRestCash();

       if(!orderService.actorCanSee(member,order)) {
           throw new IllegalArgumentException("권한이 없습니다");
       }

       return GlobalResponse.of("200","success",order.getId());
    }


    // 결제 확인
    @PostMapping("/confirm2")
    public ResponseEntity<JSONObject> confirmPayment2(@RequestBody String jsonBody) throws Exception {

        String orderId;
        String amount;
        String paymentKey;
        try {
            // 클라이언트에서 받은 JSON 요청 바디입니다.
            JSONObject jsonObject1 = new JSONObject(jsonBody);
            paymentKey = jsonObject1.getString("paymentKey");
            orderId = jsonObject1.getString("orderId");
            amount = jsonObject1.getString("amount");
        } catch (Exception e) {
            throw new RuntimeException("유효하지 않은 JSON input",e);
        }

        try {
            // 체크
            orderService.checkCanPay(orderId, Long.parseLong(amount));
        } catch(NumberFormatException e) {
            throw new RuntimeException("amount format 오류" + amount , e);
        }catch (IllegalArgumentException e) {
            throw new RuntimeException("결제 유효성 검사 실패" , e);
        }

        JSONObject obj = new JSONObject();
        obj.put("orderId", orderId);
        obj.put("amount", amount);
        obj.put("paymentKey", paymentKey);

        // TODO: 개발자센터에 로그인해서 내 결제위젯 연동 키 > 시크릿 키를 입력하세요. 시크릿 키는 외부에 공개되면 안돼요.
        // @docs https://docs.tosspayments.com/reference/using-api/api-keys
        String apiKey = tossApiKey;

        // 토스페이먼츠 API는 시크릿 키를 사용자 ID로 사용하고, 비밀번호는 사용하지 않습니다.
        // 비밀번호가 없다는 것을 알리기 위해 시크릿 키 뒤에 콜론을 추가합니다.
        // @docs https://docs.tosspayments.com/reference/using-api/authorization#%EC%9D%B8%EC%A6%9D
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((apiKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes, StandardCharsets.UTF_8);

        // 결제 승인 API를 호출하세요.
        // 결제를 승인하면 결제수단에서 금액이 차감돼요.
        // @docs https://docs.tosspayments.com/guides/payment-widget/integration#3-결제-승인하기
        URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorizations);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(obj.toString().getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();

        int code = connection.getResponseCode();
        boolean isSuccess = code == 200 ;

        // 결제 승인이 완료
        if (isSuccess) {
            orderService.payByTossPayments(orderService.findByCode(orderId).orElseThrow(() -> new RuntimeException("주문을 찾을 수 없음")), Long.parseLong(amount));
        } else {
            throw new RuntimeException("결제 승인 실패");
        }

        try (InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();
             Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {

            StringWriter writer = new StringWriter();
            char[] buffer = new char[1024];
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            String responseString = writer.toString();
            JSONObject jsonObject = new JSONObject(responseString);

            return ResponseEntity.status(code).body(jsonObject);
        } catch (IOException e) {
            throw new RuntimeException("응답 값 읽기 실패" , e);
        }
    }


    // 내 주문 리스트 페이징
    @GetMapping("/myList")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse showMyList(@RequestParam(defaultValue = "1") int page,
                                     Boolean payStatus,
                                     Boolean cancelStatus,
                                     Boolean refundStatus,
                                     Principal principal) {

        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page -1,10,Sort.by(sorts));

        Member member = memberService.findByUsername(principal.getName());
        Page<Order> orderPage = orderService.search(member,payStatus,cancelStatus,refundStatus,pageable);

        return GlobalResponse.of("200","주문 정보 리스트 반환",orderPage);
    }


    // 캐쉬로만 결제
    @PostMapping("/{id}/payByCash")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse payByCash(@PathVariable("id") long id) {
        Order order = orderService.findById(id).orElse(null);

        if(order == null) {
            throw new IllegalArgumentException("존재하지 않는 주문입니다.");
        }

        if(!orderService.canPay(order,0)) {
            throw new RuntimeException("권한이 없습니다");
        }

        orderService.payByCashOnly(order);

        return GlobalResponse.of("200","캐쉬로만 결제 완료");
    }

    // 주문 취소
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse cancelOrder(@PathVariable("id") long id, Principal principal) {
        Order order = orderService.findById(id).orElse(null);

        if(order == null) {
            throw new RuntimeException("존재하지 않는 주문입니다.");
        }

        Member member = memberService.findByUsername(principal.getName());

        if (!orderService.canCancel(member, order)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        orderService.cancel(order);

        return GlobalResponse.of("200","주문취소가 완료되었습니다.");
    }

    // 장바구니로 주문
    @PostMapping("/createFromCart")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse createFromCart(Principal principal) {
        Member member = memberService.findByUsername(principal.getName());
        Order order = orderService.createFromCart(member);
        return GlobalResponse.of("200","장바구니 주문이 완료되었습니다.");
    }

    // 단견결제
    @PostMapping("/directMakeOrder/{articleId}")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse directMakeOrder(@PathVariable("articleId") long articleId, Principal principal) {

        Article article = articleService.findById(articleId);
        Member member = memberService.findByUsername(principal.getName());
        Order order = orderService.createFromArticle(member,article);

        return GlobalResponse.of("200","단건주문이 완료되었습니다.",order.getId());
    }
}
