package com.ll.demo.member.entity;


import com.ll.demo.article.entity.Article;
import com.ll.demo.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.awt.print.Book;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Member extends BaseEntity {
    @Column(unique = true)
    private String username;
    private String password;
    private String email;
    private String nickname;
    private String refreshToken;

    private Long restCash;


    // 사용자 권한처리
    @SuppressWarnings("JpaAttributeTypeInspection")
    public List<? extends GrantedAuthority> getAuthorities() {   // Security에서 사용자의 권한 정보를 얻을 때 호출
        return getAuthoritiesAsStrList()  // 문자열 형태의 권한 목록을 가져옴
                .stream()
                .map(SimpleGrantedAuthority::new)  // 각 권한 매핑
                .toList();
    }

    @SuppressWarnings("JpaAttributeTypeInspection")  // 사용자의 권한을 문자열 형태로 반환
    public List<String> getAuthoritiesAsStrList() {  // ROLE_MEMBER 문자열을 포함한 리스트 반환
        return List.of("ROLE_MEMBER");
    }


    public boolean isAdmin() {
        return getAuthoritiesAsStrList().contains(UserRole.ADMIN.getValue());
    }

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MyArticle> myArticles = new ArrayList<>();

    public void addMyArticle(Article article) {
        MyArticle myArticle = MyArticle.builder()
                .owner(this)
                .article(article)
                .build();

        myArticles.add(myArticle);
    }

    public void removeMyArticle(Article article) {
        myArticles.removeIf(myArticle -> myArticle.getArticle().equals(article));
    }

    public boolean hasArticle(Article article) {
        return myArticles
                .stream()
                .anyMatch(myArticle -> myArticle.getArticle().equals(article));
    }
}