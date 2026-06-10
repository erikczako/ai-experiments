package com.example.shoppingcart.api;

import java.util.List;

import com.example.shoppingcart.domain.CartItem;

public record ShoppingCartResponse(String userId, List<CartItem> products) {
}
