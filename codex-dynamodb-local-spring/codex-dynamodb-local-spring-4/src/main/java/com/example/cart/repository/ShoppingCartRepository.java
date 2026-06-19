package com.example.cart.repository;

import com.example.cart.domain.CartItem;
import com.example.cart.domain.ShoppingCart;

public interface ShoppingCartRepository {

	ShoppingCart findByUserId(String userId);

	CartItem addItem(String userId, CartItem item);

	void removeItem(String userId, String productId);

	void removeCart(String userId);

}
