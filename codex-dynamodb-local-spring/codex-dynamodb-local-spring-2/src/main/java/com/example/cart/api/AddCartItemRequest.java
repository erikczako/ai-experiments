package com.example.cart.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(@NotBlank String productId,
		@NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal unitPrice) {
}
