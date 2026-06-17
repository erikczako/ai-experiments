package com.example.cart.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.cart.domain.CartProduct;
import com.example.cart.persistence.DynamoDbCartRepository;

@Service
public class ShoppingCartService {

	private final DynamoDbCartRepository repository;

	public ShoppingCartService(DynamoDbCartRepository repository) {
		this.repository = repository;
	}

	public List<CartProduct> getCart(String userId) {
		return repository.findAll(userId);
	}

	public CartProduct addProduct(String userId, String productId, BigDecimal unitPrice) {
		var product = new CartProduct(productId, unitPrice);
		repository.add(userId, product);
		return product;
	}

	public void removeProduct(String userId, String productId) {
		repository.remove(userId, productId);
	}

	public void removeCart(String userId) {
		repository.removeAll(userId);
	}

}
