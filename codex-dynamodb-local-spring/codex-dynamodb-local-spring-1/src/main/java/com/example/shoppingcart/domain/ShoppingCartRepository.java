package com.example.shoppingcart.domain;

import java.util.List;

public interface ShoppingCartRepository {

	List<CartItem> findByUserId(String userId);

	void add(String userId, CartItem item);

	void removeProduct(String userId, String productId);

	void removeCart(String userId);

}
