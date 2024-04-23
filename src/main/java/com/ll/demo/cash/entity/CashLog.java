package com.ll.demo.cash.entity;

import com.ll.demo.global.entity.BaseEntity;
import com.ll.demo.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@ToString(callSuper = true)
public class CashLog extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private EvenType eventType;
    private String relTypeCode;
    private Long relId;
    @ManyToOne
    private Member member;
    private Long price;

    public enum EvenType {
        충전__무통장입금,
        충전__토스페이먼츠,
        출금__통장입금,
        사용__토스페이먼츠_주문결제,
        사용__예치금_주문결제,
        환불__예치금_주문결제,
        작가정산__예치금;
    }
}