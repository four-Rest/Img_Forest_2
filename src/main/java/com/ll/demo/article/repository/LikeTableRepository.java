package com.ll.demo.article.repository;

import com.ll.demo.article.entity.LikeTable;
import com.ll.demo.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeTableRepository extends JpaRepository<LikeTable, Long> {

    Optional<LikeTable> getLikeByArticleIdAndMemberId(Long articleId, Long memberId);

    Page<LikeTable> getLikeTableByMemberId(Long memberId, Pageable pageable);
}
