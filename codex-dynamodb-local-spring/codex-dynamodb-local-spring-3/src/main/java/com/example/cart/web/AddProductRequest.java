package com.example.cart.web;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddProductRequest(@NotBlank String productId,
		@NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal unitPrice) {
}
