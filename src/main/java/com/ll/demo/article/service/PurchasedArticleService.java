package com.ll.demo.article.service;


import com.ll.demo.article.entity.Article;
import com.ll.demo.article.entity.PurchasedArticle;
import com.ll.demo.article.repository.PurchasedArticleRepository;
import com.ll.demo.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchasedArticleService {
    private final PurchasedArticleRepository purchasedArticleRepository;

    @Transactional
    public PurchasedArticle add(Member buyer, Article article) {
        PurchasedArticle purchasedArticle = PurchasedArticle.builder()
                .owner(buyer)
                .article(article)
                .build();
        purchasedArticleRepository.save(purchasedArticle);
        return purchasedArticle;
    }

    @Transactional
    public void delete(Member buyer,Article article) {
        purchasedArticleRepository.findTop1ByOwnerAndArticleOrderByIdDesc(buyer,article).ifPresent(purchasedArticleRepository::delete);
    }
}
