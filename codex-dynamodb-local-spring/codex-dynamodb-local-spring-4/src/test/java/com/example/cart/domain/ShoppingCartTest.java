package com.example.cart.domain;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ShoppingCartTest {

	@Test
	void totalsItemPrices() {
		ShoppingCart shoppingCart = new ShoppingCart("user-1",
				List.of(new CartItem("sku-1", new BigDecimal("3.25")), new CartItem("sku-2", new BigDecimal("4.75"))));

		assertThat(shoppingCart.totalPrice()).isEqualByComparingTo("8.00");
	}

	@Test
	void rejectsBlankUserId() {
		assertThatIllegalArgumentException().isThrownBy(() -> new ShoppingCart(" ", List.of()));
	}

}
