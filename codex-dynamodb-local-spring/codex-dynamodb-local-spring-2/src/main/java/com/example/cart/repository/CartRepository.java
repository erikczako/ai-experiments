package com.example.cart.repository;

import java.util.List;

import com.example.cart.domain.CartItem;

public interface CartRepository {

	List<CartItem> findByUserId(String userId);

	void add(String userId, CartItem item);

	void remove(String userId, String productId);

	void removeAll(String userId);

}
