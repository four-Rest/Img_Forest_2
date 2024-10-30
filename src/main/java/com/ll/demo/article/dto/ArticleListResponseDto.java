package com.ll.demo.article.dto;

import com.ll.demo.article.entity.Article;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArticleListResponseDto {
    private boolean paid;
    private String imgFilePath;
    private String imgFileName;
    private Long id;
    private int likes;

    public ArticleListResponseDto(Article article) {
        this.imgFilePath = article.getImage().getPath().substring(article.getImage().getPath().length()-11, article.getImage().getPath().length()-1);
        this.imgFileName = article.getImage().getFileName();
        this.paid = article.isPaid();
        this.id = article.getId();
        this.likes = article.getLikes();
    }
}