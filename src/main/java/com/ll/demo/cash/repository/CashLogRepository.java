package com.ll.demo.cash.repository;

import com.ll.demo.cash.entity.CashLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CashLogRepository extends JpaRepository<CashLog,Long> {
}
