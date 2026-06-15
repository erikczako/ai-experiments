package com.example.cart.domain;

import java.util.List;

public record Cart(String userId, List<CartItem> items) {

	public Cart {
		items = List.copyOf(items);
	}

}
