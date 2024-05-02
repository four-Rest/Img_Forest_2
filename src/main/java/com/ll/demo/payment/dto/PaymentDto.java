package com.ll.demo.payment.dto;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaymentDto {

    // paymentCallbackRequest + RequestPayDto

    private String username;
    private Long articleId;
    private Long amount;   // 결제 금액
    private String orderUid;
    private String impUid;  // 결제 고유 번호
}
