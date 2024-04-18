package com.ll.demo.rebate.entity;

import com.ll.demo.article.entity.Article;
import com.ll.demo.global.jpa.BaseTime;
import com.ll.demo.member.entity.Member;
import com.ll.demo.order.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


@Entity
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@ToString(callSuper = true)
public class RebateItem extends BaseTime {
    private LocalDateTime eventDate; // 판매(구매)가 발생한 날짜
    private LocalDateTime rebateDate; // 정산일
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private OrderItem orderItem; // 주문상품

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member seller; // 판매자

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member buyer; // 구매자

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Article article; // 상품

    private long payPrice; // 결제금액
    private double rebateRate; // 정산율
    private long rebatePrice; // 정산금액
    private LocalDateTime payDate;

}