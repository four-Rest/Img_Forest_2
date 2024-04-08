package com.ll.demo.order.service;

import com.ll.demo.article.entity.Article;
import com.ll.demo.article.entity.PurchasedArticle;
import com.ll.demo.article.service.PurchasedArticleService;
import com.ll.demo.cart.entity.CartItem;
import com.ll.demo.cart.service.CartService;
import com.ll.demo.cash.entity.CashLog;
import com.ll.demo.member.entity.Member;
import com.ll.demo.member.service.MemberService;
import com.ll.demo.order.entity.Order;
import com.ll.demo.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final MemberService memberService;
    private final PurchasedArticleService purchasedArticleService;


    // 기존에 구매한 article이면 구매 불가하게 만들기
    @Transactional
    public Order createFromCart(Member buyer) {
        List<CartItem> cartItems = cartService.findByBuyerOrderByIdDesc(buyer);


        Order order = Order.builder()
                .buyer(buyer)
                .build();
        // 카트에 아이템을 주문에 담고, 카트 아이템 삭제
        cartItems.stream()
                .forEach(order::addItem);
        orderRepository.save(order);
        cartItems.stream()
                .forEach(cartService::delete);
        return order;
    }

    @Transactional
    public void payByCashOnly(Order order) {
        Member buyer = order.getBuyer();
        long restCash = buyer.getRestCash();
        long payPrice = order.calcPayPrice();

        if (payPrice > restCash) {
            throw new RuntimeException("예치금이 부족합니다.");
        }
        memberService.addCash(buyer, payPrice * -1, CashLog.EvenType.사용__예치금_주문결제);

        payDone(order);
    }
    private void payDone(Order order) {
        order.setPaymentDone();

        order.getOrderItems()
                .stream()
                .forEach(orderItem -> {
                    Article article = orderItem.getArticle();
                    purchasedArticleService.add(order.getBuyer(),article);
                });
    }

    @Transactional
    public void refund(Order order) {
        long payPrice = order.calcPayPrice();

        memberService.addCash(order.getBuyer(), payPrice, CashLog.EvenType.환불__예치금_주문결제);
        order.setRefundDone();

        order.getOrderItems()
                .stream()
                .forEach(orderItem -> {
                    Article article = orderItem.getArticle();
                    purchasedArticleService.delete(order.getBuyer(), article);
                });
    }

    // 주문과 금액이 일치하지 않으면 예외처리
    public boolean checkPayPrice(Order order, long payPrice) {
        if(order.calcPayPrice() != payPrice) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다");
        }
        return true;
    }

    // PayPrice가 부족하면 부족한만큼 restCash이용해서 결제 가능, 불가능하다면 예외 발생
    public void checkCanPay(Order order, long pgPayPrice) {
        if(!canPay(order,pgPayPrice))
            throw new IllegalArgumentException("PG 결제 금액 혹은 예치금이 부족하여 결제할 수 없습니다.");
    }

    public boolean canPay(Order order,long pgPayPrice) {


        // 서버사이드에서도 올바르지 않는 결제요청을 막음.
        if(!order.isPayable()){
            return false;
        }
        long restCash = order.getBuyer().getRestCash();

        return order.calcPayPrice() <= restCash + pgPayPrice;
    }

    public void checkCanPay(String orderCode, long pgPayPrice) {
        Order order = findByCode(orderCode).orElse(null);

        if (order == null)
            throw new IllegalArgumentException("존재하지 않는 주문입니다.");

        checkCanPay(order, pgPayPrice);
    }


    // 토스페이먼츠로 결제하는 기능 , 부족한 금액은 자동으로 예치금에서 차감
    @Transactional
    public void payByTossPayments(Order order, long pgPayPrice) {
        Member buyer = order.getBuyer();
        long restCash = buyer.getRestCash();
        long payPrice = order.calcPayPrice();

        long useRestCash = payPrice - pgPayPrice;

        memberService.addCash(buyer, pgPayPrice, CashLog.EvenType.충전__토스페이먼츠);
        memberService.addCash(buyer, pgPayPrice * -1, CashLog.EvenType.사용__토스페이먼츠_주문결제);

        // transactional이기 때문에 이 작업이 안되면 위에 작업 다 취소
        if (useRestCash > 0) {
            if (useRestCash > restCash) {
                throw new RuntimeException("예치금이 부족합니다.");
            }
            memberService.addCash(buyer, useRestCash * -1, CashLog.EvenType.사용__예치금_주문결제);
        }
        payDone(order);
    }

    public Optional<Order> findById(long id) {
        return orderRepository.findById(id);
    }

    public boolean actorCanSee(Member member, Order order) {
        return order.getBuyer().equals(member);
    }

    public Optional<Order> findByCode(String code) {
        long id = Long.parseLong(code.split("__", 2)[1]);

        return findById(id);
    }


    public void payDone(String code) {
        Order order = findByCode(code).orElse(null);
        if (order == null)
            throw new IllegalArgumentException("존재하지 않는 주문입니다.");
        payDone(order);
    }

    public Page<Order> search(Member buyer, Boolean payStatus, Boolean cancelStatus, Boolean refundStatus, Pageable pageable) {

        return orderRepository.search(buyer,payStatus,cancelStatus,refundStatus,pageable);
    }


    @Transactional
    public void cancel(Order order) {
        if(!order.isCancelable()) {
            throw new RuntimeException("취소할 수 없는 주문입니다.");
        }
        order.setCancelDone();

        if(order.isPayDone()) {
            refund(order);
        }
    }

    public boolean canCancel(Member buyer, Order order) {
        return buyer.equals(order.getBuyer()) && order.isCancelable();
    }

    public Order createFromArticle(Member buyer, Article article) {
        try {
            Order order = Order.builder()
                    .buyer(buyer)
                    .article(article)
                    .build();

            if(orderRepository.existsByBuyerAndArticle(buyer,article)) {
                throw new RuntimeException("이미 구매한 이미지입니다.");
            }
            order.addItem(article);
            orderRepository.save(order);
            return order;
        } catch (RuntimeException e) {
            // 이미 구매한 상품인 경우 예외 처리
            throw new RuntimeException("이미 구매한 상품입니다.", e);
        }
    }
}