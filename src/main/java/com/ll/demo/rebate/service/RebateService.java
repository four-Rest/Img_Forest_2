package com.ll.demo.rebate.service;


import com.ll.demo.cash.entity.CashLog;
import com.ll.demo.global.util.Ut;
import com.ll.demo.member.entity.Member;
import com.ll.demo.member.service.MemberService;
import com.ll.demo.order.service.OrderService;
import com.ll.demo.rebate.entity.RebateItem;
import com.ll.demo.rebate.repository.RebateItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RebateService {

    private final RebateItemRepository rebateItemRepository;
    private final OrderService orderService;
    private final MemberService memberService;

    public List<RebateItem> findByPayDateIn(String yearMonth) {
        int monthEndDay = Ut.date.getEndDayOf(yearMonth);

        String fromDateStr = yearMonth + "-01 00:00:00.000000";
        String toDateStr = yearMonth + "-%02d 23:59:59.999999".formatted(monthEndDay);
        LocalDateTime fromDate = Ut.date.parse(fromDateStr);
        LocalDateTime toDate = Ut.date.parse(toDateStr);

        return rebateItemRepository.findByPayDateBetweenOrderByIdAsc(fromDate, toDate);
    }

    public Optional<RebateItem> findById(long id) {
        return rebateItemRepository.findById(id);
    }

    @Transactional
    public void rebate(RebateItem rebateItem) {
        if(!rebateItem.isRebateAvailable()) {
            throw new RuntimeException("정산을 할 수 없는 상태입니다.");
        }

        long rebatePrice = rebateItem.getRebatePrice();
        memberService.addCash(
                rebateItem.getSeller(),
                rebatePrice,
                CashLog.EvenType.작가정산__예치금
        );
        rebateItem.setRebateDone();
    }

    public boolean canRebate(Member actor, RebateItem rebateItem) {
        return actor.isAdmin() && rebateItem.isRebateAvailable();
    }

}
