package com.example.cart.domain;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.util.StringUtils;

public record ShoppingCart(String userId, List<CartItem> items) {

	public ShoppingCart {
		if (!StringUtils.hasText(userId)) {
			throw new IllegalArgumentException("userId must not be blank");
		}
		userId = userId.trim();
		items = List.copyOf(items);
	}

	public BigDecimal totalPrice() {
		return this.items.stream().map(CartItem::unitPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

}
