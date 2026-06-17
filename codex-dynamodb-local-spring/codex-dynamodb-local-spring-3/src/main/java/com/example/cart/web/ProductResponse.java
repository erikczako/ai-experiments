package com.example.cart.web;

import java.math.BigDecimal;

import com.example.cart.domain.CartProduct;

public record ProductResponse(String productId, BigDecimal unitPrice) {

	static ProductResponse from(CartProduct product) {
		return new ProductResponse(product.productId(), product.unitPrice());
	}

}
