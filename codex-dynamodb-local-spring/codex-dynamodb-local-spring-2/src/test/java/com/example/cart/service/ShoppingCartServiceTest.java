package com.example.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import com.example.cart.domain.CartItem;
import com.example.cart.repository.CartRepository;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTest {

	@Mock
	private CartRepository repository;

	private ShoppingCartService service;

	private final CartItem item = new CartItem("product-1", new BigDecimal("12.50"));

	@BeforeEach
	void setUp() {
		service = new ShoppingCartService(repository);
	}

	@Test
	void delegatesAllCartOperations() {
		when(repository.findByUserId("user-1")).thenReturn(List.of(item));

		assertThat(service.getCart("user-1").items()).containsExactly(item);

		service.addProduct("user-1", item);
		service.removeProduct("user-1", "product-1");
		service.removeCart("user-1");

		verify(repository).add("user-1", item);
		verify(repository).remove("user-1", "product-1");
		verify(repository).removeAll("user-1");
	}

}
