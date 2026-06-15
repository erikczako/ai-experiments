package com.example.cart.service;

import org.springframework.stereotype.Service;

import com.example.cart.domain.Cart;
import com.example.cart.domain.CartItem;
import com.example.cart.repository.CartRepository;

@Service
public class ShoppingCartService {

	private final CartRepository repository;

	public ShoppingCartService(CartRepository repository) {
		this.repository = repository;
	}

	public Cart getCart(String userId) {
		return new Cart(userId, repository.findByUserId(userId));
	}

	public void addProduct(String userId, CartItem item) {
		repository.add(userId, item);
	}

	public void removeProduct(String userId, String productId) {
		repository.remove(userId, productId);
	}

	public void removeCart(String userId) {
		repository.removeAll(userId);
	}

}
