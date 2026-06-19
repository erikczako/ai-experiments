package com.example.cart.web;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import com.example.cart.domain.CartItem;
import com.example.cart.service.ShoppingCartService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

@RestController
@RequestMapping("/api/v1/cart")
class ShoppingCartController {

	private static final String USER_ID_HEADER = "X-User-Id";

	private final ShoppingCartService shoppingCartService;

	ShoppingCartController(ShoppingCartService shoppingCartService) {
		this.shoppingCartService = shoppingCartService;
	}

	@GetMapping
	ShoppingCartResponse getCart(@RequestHeader(USER_ID_HEADER) @NotBlank String userId) {
		return ShoppingCartResponse.from(this.shoppingCartService.getCart(normalize(userId)));
	}

	@PostMapping("/items")
	ResponseEntity<CartItemResponse> addItem(@RequestHeader(USER_ID_HEADER) @NotBlank String userId,
			@Valid @RequestBody AddCartItemRequest request) {
		CartItem item = this.shoppingCartService.addItem(normalize(userId), request.toCartItem());
		return ResponseEntity.created(locationFor(item)).body(CartItemResponse.from(item));
	}

	@DeleteMapping("/items/{productId}")
	ResponseEntity<Void> removeItem(@RequestHeader(USER_ID_HEADER) @NotBlank String userId,
			@PathVariable @NotBlank String productId) {
		this.shoppingCartService.removeItem(normalize(userId), normalize(productId));
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping
	ResponseEntity<Void> removeCart(@RequestHeader(USER_ID_HEADER) @NotBlank String userId) {
		this.shoppingCartService.removeCart(normalize(userId));
		return ResponseEntity.noContent().build();
	}

	private URI locationFor(CartItem item) {
		return URI.create("/api/v1/cart/items/" + UriUtils.encodePathSegment(item.productId(), StandardCharsets.UTF_8));
	}

	private String normalize(String value) {
		if (!StringUtils.hasText(value)) {
			throw new IllegalArgumentException("Value must not be blank");
		}
		return value.trim();
	}

}
