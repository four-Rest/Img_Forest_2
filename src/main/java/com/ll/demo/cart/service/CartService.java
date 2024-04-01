package com.ll.demo.cart.service;

import com.ll.demo.article.entity.Article;
import com.ll.demo.cart.entity.CartItem;
import com.ll.demo.cart.repository.CartItemRepository;
import com.ll.demo.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    private final CartItemRepository cartItemRepository;

    @Transactional
    public CartItem addItem(Member buyer, Article article) {

        if(buyer.hasArticle(article)) {
            throw new IllegalArgumentException("이미 구매한 이미지입니다.");
        }
        CartItem cartItem = CartItem.builder()
                .buyer(buyer)
                .article(article)
                .build();

        cartItemRepository.save(cartItem);

        return cartItem;
    }

    public int removeItem(Member buyer, Article article) {
        List<CartItem> cartItems = cartItemRepository.findCartItemByArticleAndBuyer(article,buyer);
        cartItemRepository.deleteAll(cartItems);
        return cartItems.size();
    }


    public List<CartItem> findByBuyer(Member buyer) {
        return cartItemRepository.findByBuyer(buyer);
    }

    public void delete(CartItem cartItem) {
        cartItemRepository.delete(cartItem);
    }

    public boolean canAdd(Member buyer,Article article){
        if(buyer == null) {
            return false;
        }
        return !cartItemRepository.existsByBuyerAndArticle(buyer, article);
    }
    public boolean canRemove(Member buyer, Article article) {
        if (buyer == null) return false;

        return cartItemRepository.existsByBuyerAndArticle(buyer, article);
    }

    public List<CartItem> findByBuyerOrderByIdDesc(Member buyer) {
        return cartItemRepository.findByBuyer(buyer);
    }

    public boolean canDirectMakeOrder(Member buyer,Article article) {
        return canAdd(buyer,article);
    }
}