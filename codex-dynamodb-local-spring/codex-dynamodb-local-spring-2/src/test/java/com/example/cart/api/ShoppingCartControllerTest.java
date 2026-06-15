package com.example.cart.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.cart.domain.Cart;
import com.example.cart.domain.CartItem;
import com.example.cart.service.ShoppingCartService;

@ExtendWith(MockitoExtension.class)
class ShoppingCartControllerTest {

	@Mock
	private ShoppingCartService service;

	private ShoppingCartController controller;

	@BeforeEach
	void setUp() {
		controller = new ShoppingCartController(service);
	}

	@Test
	void delegatesAllEndpoints() {
		Cart cart = new Cart("user-1", List.of());
		when(service.getCart("user-1")).thenReturn(cart);

		assertThat(controller.getCart("user-1")).isSameAs(cart);

		AddCartItemRequest request = new AddCartItemRequest("product-1", new BigDecimal("12.50"));
		controller.addProduct("user-1", request);
		controller.removeProduct("user-1", "product-1");
		controller.removeCart("user-1");

		verify(service).addProduct("user-1", new CartItem("product-1", new BigDecimal("12.50")));
		verify(service).removeProduct("user-1", "product-1");
		verify(service).removeCart("user-1");
	}

}
