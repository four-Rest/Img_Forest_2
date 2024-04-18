package com.ll.demo.order.repository;


import com.ll.demo.order.entity.OrderItem;
import com.ll.demo.rebate.entity.RebateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {
    List<OrderItem> findByOrderPayDateBetweenAndOrderRefundDateAndRebateItemOrderByIdDesc(
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime refundDate,
            RebateItem rebateItem
    );
}
