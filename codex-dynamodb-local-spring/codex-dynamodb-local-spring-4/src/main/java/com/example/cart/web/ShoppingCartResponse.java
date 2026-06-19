package com.example.cart.web;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import com.example.cart.domain.CartItem;
import com.example.cart.domain.ShoppingCart;

public record ShoppingCartResponse(String userId, List<CartItemResponse> items, BigDecimal totalPrice) {

	static ShoppingCartResponse from(ShoppingCart shoppingCart) {
		return new ShoppingCartResponse(shoppingCart.userId(),
				shoppingCart.items()
					.stream()
					.sorted(Comparator.comparing(CartItem::productId))
					.map(CartItemResponse::from)
					.toList(),
				shoppingCart.totalPrice());
	}

}
