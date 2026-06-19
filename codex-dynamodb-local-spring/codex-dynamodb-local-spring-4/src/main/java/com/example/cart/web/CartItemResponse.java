package com.example.cart.web;

import java.math.BigDecimal;

import com.example.cart.domain.CartItem;

public record CartItemResponse(String productId, BigDecimal unitPrice) {

	static CartItemResponse from(CartItem item) {
		return new CartItemResponse(item.productId(), item.unitPrice());
	}

}
