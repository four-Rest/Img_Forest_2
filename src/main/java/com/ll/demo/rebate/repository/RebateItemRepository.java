package com.ll.demo.rebate.repository;


import com.ll.demo.rebate.entity.RebateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RebateItemRepository extends JpaRepository<RebateItem, Long> {

    List<RebateItem> findByPayDateBetweenOrderByIdAsc(LocalDateTime fromDate, LocalDateTime toDate);
}