package com.ll.demo.rebate.service;


import com.ll.demo.order.entity.OrderItem;
import com.ll.demo.order.service.OrderService;
import com.ll.demo.rebate.entity.RebateItem;
import com.ll.demo.rebate.repository.RebateItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RebateService {

    private final RebateItemRepository rebateItemRepository;
    private final OrderService orderService;

    @Transactional
    public void make(String yearMonth) {

        String[] yearMonthBits = yearMonth.split("-", 2);

        int year = Integer.parseInt(yearMonthBits[0]);
        int month = Integer.parseInt(yearMonthBits[1]);

        YearMonth yearMonth_ = YearMonth.of(year, month); // 연월 지정
        LocalDateTime startDate = yearMonth_.atDay(1).atStartOfDay(); // 해당 월의 첫 날 00:00
        LocalDateTime endDate = yearMonth_.atEndOfMonth().atTime(23, 59, 59, 999999999); // 해당 월의 마지막 날 23:59:59.999999999

        List<OrderItem> orderItems = orderService.findNotRefundedByPayDateBetween(startDate, endDate);
        orderItems
                .stream()
                .forEach(orderItem -> {
                    RebateItem rebateItem = RebateItem.builder()
                            .eventDate(orderItem.getOrder().getPayDate())
                            .rebateRate(orderItem.getRebateRate())
                            .payPrice(orderItem.getPayPrice())
                            .rebatePrice((long) Math.ceil(orderItem.getPayPrice() * orderItem.getRebateRate()))
                            .orderItem(orderItem)
                            .buyer(orderItem.getOrder().getBuyer())
                            .seller(orderItem.getArticle().getMember())
                            .article(orderItem.getArticle())
                            .build();

                    rebateItemRepository.save(rebateItem);
                });
    }
}
