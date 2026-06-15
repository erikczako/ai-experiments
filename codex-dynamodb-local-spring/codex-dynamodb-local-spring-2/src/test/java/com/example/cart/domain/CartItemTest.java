package com.example.cart.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class CartItemTest {

	@Test
	void validatesAndCreatesImmutableDomainObjects() {
		CartItem item = new CartItem("product-1", new BigDecimal("12.50"));
		ArrayList<CartItem> mutableItems = new ArrayList<>();
		mutableItems.add(item);

		Cart cart = new Cart("user-1", mutableItems);
		mutableItems.clear();

		assertThat(cart.items()).containsExactly(item);
		assertThatThrownBy(() -> cart.items().clear()).isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> new CartItem(" ", BigDecimal.ONE)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("productId must not be blank");
		assertThatThrownBy(() -> new CartItem(null, BigDecimal.ONE)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new CartItem("product-1", BigDecimal.ZERO))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("unitPrice must be greater than zero");
		assertThatThrownBy(() -> new CartItem("product-1", null)).isInstanceOf(IllegalArgumentException.class);
	}

}
