package com.example.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.cart.domain.CartProduct;
import com.example.cart.persistence.DynamoDbCartRepository;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTest {

	@Mock
	private DynamoDbCartRepository repository;

	@Test
	void delegatesAllCartOperations() {
		var service = new ShoppingCartService(repository);
		var product = new CartProduct("sku-1", new BigDecimal("12.50"));
		when(repository.findAll("user-1")).thenReturn(List.of(product));

		assertThat(service.getCart("user-1")).containsExactly(product);
		assertThat(service.addProduct("user-1", "sku-1", new BigDecimal("12.50"))).isEqualTo(product);
		service.removeProduct("user-1", "sku-1");
		service.removeCart("user-1");

		verify(repository).add("user-1", product);
		verify(repository).remove("user-1", "sku-1");
		verify(repository).removeAll("user-1");
	}

}
