package com.ll.demo.cart.dto;

import com.ll.demo.cart.entity.CartItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartDto {
    private long totalPrice;
    private List<Long> articleIds;
}
