package com.example.shoppingcart.domain;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ShoppingCartService {

	private final ShoppingCartRepository repository;

	public ShoppingCartService(ShoppingCartRepository repository) {
		this.repository = repository;
	}

	public List<CartItem> getCart(String userId) {
		return repository.findByUserId(userId);
	}

	public CartItem addProduct(String userId, String productId, BigDecimal unitPrice) {
		CartItem item = new CartItem(productId, unitPrice);
		repository.add(userId, item);
		return item;
	}

	public void removeProduct(String userId, String productId) {
		repository.removeProduct(userId, productId);
	}

	public void removeCart(String userId) {
		repository.removeCart(userId);
	}

}
