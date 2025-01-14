package com.ll.demo.article.repository;

import com.ll.demo.article.entity.Article;
import com.ll.demo.article.entity.ArticleTag;
import com.ll.demo.article.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {
    ArticleTag findByArticleAndTag(Article article, Tag tag);
    Set<ArticleTag> findByArticle(Article article);
}
