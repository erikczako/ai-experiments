package com.example.cart.repository;

public class DuplicateProductException extends RuntimeException {

	public DuplicateProductException(String productId) {
		super("Product " + productId + " is already in the shopping cart.");
	}

}
