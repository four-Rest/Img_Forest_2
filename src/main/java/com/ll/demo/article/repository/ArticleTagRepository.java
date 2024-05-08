package com.ll.demo.article.repository;

import com.ll.demo.article.entity.Article;
import com.ll.demo.article.entity.ArticleTag;
import com.ll.demo.article.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {
    ArticleTag findByArticleAndTag(Article article, Tag tag);
}
