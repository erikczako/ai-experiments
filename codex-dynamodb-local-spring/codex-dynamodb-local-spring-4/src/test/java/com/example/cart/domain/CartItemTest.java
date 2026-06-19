package com.example.cart.domain;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CartItemTest {

	@Test
	void trimsProductId() {
		CartItem item = new CartItem(" sku-1 ", new BigDecimal("10.00"));

		assertThat(item.productId()).isEqualTo("sku-1");
	}

	@Test
	void rejectsBlankProductId() {
		assertThatIllegalArgumentException().isThrownBy(() -> new CartItem(" ", new BigDecimal("10.00")));
	}

	@Test
	void rejectsNonPositivePrice() {
		assertThatIllegalArgumentException().isThrownBy(() -> new CartItem("sku-1", BigDecimal.ZERO));
	}

	@Test
	void rejectsPriceWithMoreThanTwoDecimalPlaces() {
		assertThatIllegalArgumentException().isThrownBy(() -> new CartItem("sku-1", new BigDecimal("10.001")));
	}

}
