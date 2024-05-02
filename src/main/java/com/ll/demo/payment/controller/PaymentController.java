//package com.ll.demo.payment.controller;
//
//
//import com.ll.demo.global.response.GlobalResponse;
//import com.ll.demo.member.entity.Member;
//import com.ll.demo.member.service.MemberService;
//import com.ll.demo.order.dto.OrderRequestDto;
//import com.ll.demo.payment.dto.PaymentDto;
//import com.ll.demo.payment.service.PaymentService;
//import com.siot.IamportRestClient.IamportClient;
//import com.siot.IamportRestClient.exception.IamportResponseException;
//import com.siot.IamportRestClient.response.IamportResponse;
//import com.siot.IamportRestClient.response.Payment;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.IOException;
//import java.security.Principal;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/payment")
//@Slf4j
//@Tag(name = "Payment", description = "Payment API")
//@ApiResponses(value = {
//        @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.", content = @Content(mediaType = "application/json")),
//        @ApiResponse(responseCode = "400", description = "요청에 실패했습니다.", content = @Content(mediaType = "application/json")),
//        @ApiResponse(responseCode = "401", description = "인증이 필요합니다.", content = @Content(mediaType = "application/json")),
//        @ApiResponse(responseCode = "403", description = "요청이 거부되었습니다.", content = @Content(mediaType = "application/json")),
//        @ApiResponse(responseCode = "404", description = "리소스를 서버에서 찾을 수 없습니다.", content = @Content(mediaType = "application/json"))
//})
//public class PaymentController {
//
//    private final PaymentService paymentService;
//
//
//    // 결제 요청
//    @PreAuthorize("isAuthenticated()")
//    @PostMapping("")
//    public GlobalResponse<IamportResponse<Payment>> validationPayment(@RequestBody PaymentDto paymentDto) {
//        IamportResponse<Payment> iamportResponse =  paymentService.paymentByCallback(paymentDto);
//        log.info("결제 응답={}", iamportResponse.getResponse().toString());
//
//        return GlobalResponse.of("200","결제요청완료",iamportResponse);
//
//    }
//
//    // 결제 조회
//    @PreAuthorize("isAuthenticated()")
//    @GetMapping("/payment/{id}")
//    public GlobalResponse paymentPage(@PathVariable("id") String id) {
//        PaymentDto paymentDto = paymentService.findRequestDto(id);
//
//        return GlobalResponse.of("200","결제 조회 반환",paymentDto);
//
//    }
//
//
//
//
//
//}