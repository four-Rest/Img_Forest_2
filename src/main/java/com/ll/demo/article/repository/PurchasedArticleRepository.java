package com.ll.demo.article.repository;


import com.ll.demo.article.entity.Article;
import com.ll.demo.article.entity.PurchasedArticle;
import com.ll.demo.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchasedArticleRepository extends JpaRepository<PurchasedArticle,Long> {


    Optional<PurchasedArticle> findTop1ByOwnerAndArticleOrderByIdDesc(Member buyer, Article article);

}
