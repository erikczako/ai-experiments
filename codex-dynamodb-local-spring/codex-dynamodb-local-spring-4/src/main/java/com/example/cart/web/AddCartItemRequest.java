package com.example.cart.web;

import java.math.BigDecimal;

import com.example.cart.domain.CartItem;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(@NotBlank String productId, @NotNull @DecimalMin(value = "0.00",
		inclusive = false) @Digits(integer = 17, fraction = 2) BigDecimal unitPrice) {

	CartItem toCartItem() {
		return new CartItem(this.productId, this.unitPrice);
	}

}
