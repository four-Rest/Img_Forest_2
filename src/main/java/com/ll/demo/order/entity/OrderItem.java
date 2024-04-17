package com.ll.demo.order.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ll.demo.article.entity.Article;
import com.ll.demo.global.entity.BaseEntity;
import jakarta.persistence.Entity;
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
public class OrderItem extends BaseEntity {

    @JsonIgnore
    @ManyToOne
    private Order order;

    @JsonIgnore
    @ManyToOne
    private Article article;

    private double rebateRate;  // 판매되었을 당시 정산율

    private long payPrice;  // 판매되었을 당시 가격

    public long getPayPrice() {
        return article.getPrice();
    }

    public void setPaymentDone() {
        Article article = this.article;
        if(article !=null) {
            order.getBuyer().addMyArticle(article);
        }
    }

    public void setCancelDone() {
    }

    public void setRefundDone() {
        Article article = this.article;
        if(article !=null) {
            order.getBuyer().removeMyArticle(article);
        }
    }
}