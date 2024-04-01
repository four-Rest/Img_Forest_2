package com.ll.demo.cart.entity;

import com.ll.demo.article.entity.Article;
import com.ll.demo.global.entity.BaseEntity;
import com.ll.demo.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@ToString(callSuper = true)
public class CartItem extends BaseEntity {


    @ManyToOne
    private Member buyer;
    @ManyToOne
    private Article article;
}