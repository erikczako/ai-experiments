package com.example.cart.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.util.StringUtils;

public record CartItem(String productId, BigDecimal unitPrice) {

	public CartItem {
		if (!StringUtils.hasText(productId)) {
			throw new IllegalArgumentException("productId must not be blank");
		}
		if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("unitPrice must be greater than zero");
		}
		try {
			unitPrice = unitPrice.setScale(2, RoundingMode.UNNECESSARY);
		}
		catch (ArithmeticException ex) {
			throw new IllegalArgumentException("unitPrice must use at most two decimal places", ex);
		}
		productId = productId.trim();
	}

}
