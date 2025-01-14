package com.ll.demo.article.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.ll.demo.comment.entity.Comment;
import com.ll.demo.global.entity.BaseEntity;
import com.ll.demo.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Set;

@Entity
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Article extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne
    private Member member;

    private boolean paid;

    private Long price;

    private int likes;

    @JsonIgnore
    @OneToOne(mappedBy = "article", cascade = CascadeType.REMOVE)
    private Image image;

    @Setter
    @OneToMany(mappedBy = "article", cascade = CascadeType.REMOVE)
    private Set<ArticleTag> articleTags;

    @OneToMany(mappedBy = "article", cascade = CascadeType.REMOVE)
    private List<Comment> commentList;


    public void setImage(Image image) {
        this.image = image;
    }

    public void modifyContent(String content) {
        this.content = content;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }


}
