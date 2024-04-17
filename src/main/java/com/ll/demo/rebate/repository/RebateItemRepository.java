package com.ll.demo.rebate.repository;


import com.ll.demo.rebate.entity.RebateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RebateItemRepository extends JpaRepository<RebateItem, Long> {
}