package com.example.shoppingcart.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class ShoppingCartServiceTests {

	private final ShoppingCartRepository repository = mock(ShoppingCartRepository.class);

	private final ShoppingCartService service = new ShoppingCartService(this.repository);

	@Test
	void delegatesCartOperationsToRepository() {
		CartItem item = new CartItem("product-1", new BigDecimal("12.50"));
		when(this.repository.findByUserId("user-1")).thenReturn(List.of(item));

		assertThat(this.service.getCart("user-1")).containsExactly(item);
		assertThat(this.service.addProduct("user-1", "product-1", new BigDecimal("12.50"))).isEqualTo(item);
		this.service.removeProduct("user-1", "product-1");
		this.service.removeCart("user-1");

		verify(this.repository).add("user-1", item);
		verify(this.repository).removeProduct("user-1", "product-1");
		verify(this.repository).removeCart("user-1");
	}

}
