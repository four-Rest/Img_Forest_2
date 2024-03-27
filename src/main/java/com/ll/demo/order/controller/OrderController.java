package com.ll.demo.order.controller;


import com.ll.demo.global.response.GlobalResponse;
import com.ll.demo.member.entity.Member;
import com.ll.demo.member.service.MemberService;
import com.ll.demo.order.entity.Order;
import com.ll.demo.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final MemberService memberService;

    @Value("${custom.tossPayments.widget.secretKey")
    private String tossApiKey;

    // 주문 상세
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public GlobalResponse showDetail(@PathVariable long id, Principal principal) {
       Order order = orderService.findById(id).orElse(null);

       if (order == null) {
           throw new IllegalArgumentException("주문을 찾을 수 없습니다");
       }
       Member member = memberService.findByUsername(principal.getName());

       long restCash = member.getRestCash();

       if(!orderService.actorCanSee(member,order)) {
           throw new IllegalArgumentException("권한이 없습니다");
       }

       return GlobalResponse.of("200","success",order);
    }


    // 결제 확인
    @PostMapping("/confirm2")
    public ResponseEntity<JSONObject> confirmPayment2(@RequestBody String jsonBody) throws Exception {

        JSONParser parser = new JSONParser();
        String orderId;
        String amount;
        String paymentKey;
        try {
            // 클라이언트에서 받은 JSON 요청 바디입니다.
            JSONObject requestData = (JSONObject) parser.parse(jsonBody);
            paymentKey = (String) requestData.get("paymentKey");
            orderId = (String) requestData.get("orderId");
            amount = (String) requestData.get("amount");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        // 체크
        orderService.checkCanPay(orderId, Long.parseLong(amount));

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
        byte[] encodedBytes = encoder.encode((apiKey + ":").getBytes("UTF-8"));
        String authorizations = "Basic " + new String(encodedBytes, 0, encodedBytes.length);

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
        outputStream.write(obj.toString().getBytes("UTF-8"));

        int code = connection.getResponseCode();
        boolean isSuccess = code == 200 ? true : false;

        // 결제 승인이 완료
        if (isSuccess) {
            orderService.payByTossPayments(orderService.findByCode(orderId).get(), Long.parseLong(amount));
        } else {
            throw new RuntimeException("결제 승인 실패");
        }

        InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();

        // TODO: 결제 성공 및 실패 비즈니스 로직을 구현하세요.
        Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
        JSONObject jsonObject = (JSONObject) parser.parse(reader);
        responseStream.close();

        return ResponseEntity.status(code).body(jsonObject);
    }

    @GetMapping("/myList")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse showMyList(Principal principal) {

        Member member = memberService.findByUsername(principal.getName());
        List<Order> orderList = orderService.findByBuyer(member);

        return GlobalResponse.of("200","주문 정보 리스트 반환",orderList);
    }

    @PostMapping("/{id}/payByCash")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse payByCash(@PathVariable long id) {
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
}
