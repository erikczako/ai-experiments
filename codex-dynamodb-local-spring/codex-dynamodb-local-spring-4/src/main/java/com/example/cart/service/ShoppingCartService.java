package com.example.cart.service;

import com.example.cart.domain.CartItem;
import com.example.cart.domain.ShoppingCart;
import com.example.cart.repository.ShoppingCartRepository;

import org.springframework.stereotype.Service;

@Service
public class ShoppingCartService {

	private final ShoppingCartRepository shoppingCartRepository;

	public ShoppingCartService(ShoppingCartRepository shoppingCartRepository) {
		this.shoppingCartRepository = shoppingCartRepository;
	}

	public ShoppingCart getCart(String userId) {
		return this.shoppingCartRepository.findByUserId(userId);
	}

	public CartItem addItem(String userId, CartItem item) {
		return this.shoppingCartRepository.addItem(userId, item);
	}

	public void removeItem(String userId, String productId) {
		this.shoppingCartRepository.removeItem(userId, productId);
	}

	public void removeCart(String userId) {
		this.shoppingCartRepository.removeCart(userId);
	}

}
