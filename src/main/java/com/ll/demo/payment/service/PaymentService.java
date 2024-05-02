//package com.ll.demo.payment.service;
//
//
//import com.ll.demo.article.entity.Article;
//import com.ll.demo.article.repository.ArticleRepository;
//import com.ll.demo.member.entity.Member;
//import com.ll.demo.order.dto.OrderRequestDto;
//import com.ll.demo.order.entity.Order;
//import com.ll.demo.order.repository.OrderRepository;
//import com.ll.demo.payment.dto.PaymentDto;
//import com.ll.demo.payment.enums.PayStatus;
//import com.ll.demo.payment.repository.PaymentRepository;
//import com.siot.IamportRestClient.IamportClient;
//import com.siot.IamportRestClient.exception.IamportResponseException;
//import com.siot.IamportRestClient.request.CancelData;
//import com.siot.IamportRestClient.response.IamportResponse;
//import com.siot.IamportRestClient.response.Payment;
//
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.IOException;
//import java.math.BigDecimal;
//
//
//@Service
//@Transactional
//@RequiredArgsConstructor
//public class PaymentService {
//    private final OrderRepository orderRepository;
//    private final PaymentRepository paymentRepository;
//    private IamportClient iamportClient;
//
//    public IamportResponse<Payment> paymentByCallback(PaymentDto paymentDto) {
//
//        try {
//            // 결제 단건 조회
//            IamportResponse<Payment> iamportResponse = iamportClient.paymentByImpUid(paymentDto.getImpUid());
//            // 주문 내역 조회
//            Order order = orderRepository.findOrderAndPayment(paymentDto.getOrderUid())
//                    .orElseThrow(() -> new IllegalArgumentException("주문 내역이 존재하지 않습니다"));
//
//            // 결제 완료가 아니면
//            if(!iamportResponse.getResponse().getStatus().equals("paid")) {
//                // 주문, 결제 삭제
//                orderRepository.delete(order);
//                paymentRepository.delete(order.getPayment());
//                throw new RuntimeException("결제 미완료");
//            }
//            Long price = order.getPayment().getPrice();
//            int iamportPrice = iamportResponse.getResponse().getAmount().intValue();
//            // 결제 금액 검증
//            if(iamportPrice != price) {
//                // 결제 금액이 다르면 주문, 결제 삭제
//                orderRepository.delete(order);
//                paymentRepository.delete(order.getPayment());
//                // 결제금액 위변조로 의심되는 결제금액을 취소(아임포트)
//                iamportClient.cancelPaymentByImpUid(new CancelData(iamportResponse.getResponse().getImpUid(), true, new BigDecimal(iamportPrice)));
//
//                throw new RuntimeException("결제금액 위변조 의심");
//            }
//
//            // 결제 상태 변경
//            order.getPayment().changePaymentBySuccess(PayStatus.SUCCESS, iamportResponse.getResponse().getImpUid());
//
//            return iamportResponse;
//
//        } catch (IamportResponseException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public PaymentDto findRequestDto(String orderUid) {
//        Order order = orderRepository.findOrderAndPaymentAndMember(orderUid)
//                .orElseThrow(() -> new IllegalArgumentException("주문이 없습니다"));
//
//        return PaymentDto.builder()
//                .orderUid(order.getOrderUid())
//                .amount(order.getPayment().getPrice())
//                .username(order.getMember().getUsername())
//                .articleId(order.getArticle().getId())
//                .build();
//    }
//
//}
