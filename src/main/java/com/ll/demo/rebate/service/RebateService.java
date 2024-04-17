package com.ll.demo.rebate.service;


import com.ll.demo.order.entity.OrderItem;
import com.ll.demo.rebate.entity.RebateItem;
import com.ll.demo.rebate.repository.RebateItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RebateService {

    private final RebateItemRepository rebateItemRepository;

    @Transactional
    public void make(List<OrderItem> orderItems) {
        orderItems
                .stream()
                .forEach(orderItem -> {
                    RebateItem rebateItem = RebateItem.builder()
                            .eventDate(orderItem.getOrder().getCreatedTime())
                            .rebateRate(orderItem.getRebateRate())
                            .payPrice(orderItem.getPayPrice())
                            .orderItem(orderItem)
                            .buyer(orderItem.getOrder().getBuyer())
                            .seller(orderItem.getArticle().getMember())
                            .article(orderItem.getArticle())
                            .build();

                    rebateItemRepository.save(rebateItem);
                });
    }
}
