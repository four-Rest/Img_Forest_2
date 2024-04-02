package com.ll.demo.order.repository;

import com.ll.demo.article.entity.Article;
import com.ll.demo.member.entity.Member;
import com.ll.demo.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {
    boolean existsByBuyerAndArticle(Member buyer, Article article);
}
