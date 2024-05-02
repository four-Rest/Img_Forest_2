package com.ll.demo.order.repository;

import com.ll.demo.global.util.Ut;
import com.ll.demo.member.entity.Member;
import com.ll.demo.order.entity.Order;
import com.ll.demo.order.entity.QOrder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;


@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public Page<Order> search(Member buyer, Boolean payStatus, Boolean cancelStatus, Boolean refundStatus, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if(buyer != null) {
            builder.and(QOrder.order.buyer.eq(buyer));
        }
        if(Ut.match.isTrue(payStatus)) {
            builder.and(QOrder.order.payDate.isNotNull());
        }
        if(Ut.match.isFalse(payStatus)) {
            builder.and(QOrder.order.payDate.isNull());
        }
        if(Ut.match.isTrue(cancelStatus)) {
            builder.and(QOrder.order.cancelDate.isNotNull());
        }
        if(Ut.match.isFalse(cancelStatus)) {
            builder.and(QOrder.order.cancelDate.isNull());
        }

        if(Ut.match.isTrue(refundStatus)) {
            builder.and(QOrder.order.refundDate.isNotNull());
        }
        if(Ut.match.isFalse(refundStatus)) {
            builder.and(QOrder.order.refundDate.isNull());
        }

        JPAQuery<Order> ordersQuery = jpaQueryFactory
                .select(QOrder.order)
                .from(QOrder.order)
                .where(builder);

        for (Sort.Order o : pageable.getSort()) {
            PathBuilder pathBuilder = new PathBuilder(QOrder.order.getType(), QOrder.order.getMetadata());
            ordersQuery.orderBy(new OrderSpecifier(o.isAscending() ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC, pathBuilder.get(o.getProperty())));
        }


        ordersQuery.offset(pageable.getOffset()).limit(pageable.getPageSize());

        JPAQuery<Long> totalQuery = jpaQueryFactory
                .select(QOrder.order.count())
                .from(QOrder.order)
                .where(builder);

        return PageableExecutionUtils.getPage(ordersQuery.fetch(), pageable, totalQuery::fetchOne);
    }





}

