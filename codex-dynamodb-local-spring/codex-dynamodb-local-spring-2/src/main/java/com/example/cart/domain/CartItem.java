package com.example.cart.domain;

import java.math.BigDecimal;

public record CartItem(String productId, BigDecimal unitPrice) {

	public CartItem {
		if (productId == null || productId.isBlank()) {
			throw new IllegalArgumentException("productId must not be blank");
		}
		if (unitPrice == null || unitPrice.signum() <= 0) {
			throw new IllegalArgumentException("unitPrice must be greater than zero");
		}
	}

}
