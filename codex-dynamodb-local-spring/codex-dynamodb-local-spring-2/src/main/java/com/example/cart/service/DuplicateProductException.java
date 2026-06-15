package com.example.cart.service;

public class DuplicateProductException extends RuntimeException {

	public DuplicateProductException(String userId, String productId, Throwable cause) {
		super("Product '%s' already exists in cart for user '%s'".formatted(productId, userId), cause);
	}

}
