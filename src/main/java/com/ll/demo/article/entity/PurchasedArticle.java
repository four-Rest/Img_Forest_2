package com.ll.demo.article.entity;


import com.ll.demo.global.jpa.BaseTime;
import com.ll.demo.member.entity.Member;
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
public class PurchasedArticle extends BaseTime {
    @ManyToOne
    private Member owner;

    @ManyToOne
    private Article article;

}
