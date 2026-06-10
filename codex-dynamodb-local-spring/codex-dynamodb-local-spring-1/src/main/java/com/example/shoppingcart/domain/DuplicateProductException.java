package com.example.shoppingcart.domain;

public class DuplicateProductException extends RuntimeException {

	public DuplicateProductException(String productId) {
		super("Product '%s' is already in the shopping cart".formatted(productId));
	}

}
