package com.example.cart.web;

import java.util.List;

import com.example.cart.domain.CartProduct;

public record CartResponse(String userId, List<ProductResponse> products) {

	static CartResponse from(String userId, List<CartProduct> products) {
		return new CartResponse(userId, products.stream().map(ProductResponse::from).toList());
	}

}
