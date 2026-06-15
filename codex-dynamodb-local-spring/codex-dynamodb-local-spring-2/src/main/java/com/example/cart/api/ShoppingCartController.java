package com.example.cart.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.cart.domain.Cart;
import com.example.cart.domain.CartItem;
import com.example.cart.service.ShoppingCartService;

@Validated
@RestController
@RequestMapping("/carts/{userId}")
public class ShoppingCartController {

	private final ShoppingCartService service;

	public ShoppingCartController(ShoppingCartService service) {
		this.service = service;
	}

	@GetMapping
	public Cart getCart(@PathVariable @NotBlank String userId) {
		return service.getCart(userId);
	}

	@PostMapping("/items")
	@ResponseStatus(HttpStatus.CREATED)
	public void addProduct(@PathVariable @NotBlank String userId, @Valid @RequestBody AddCartItemRequest request) {
		service.addProduct(userId, new CartItem(request.productId(), request.unitPrice()));
	}

	@DeleteMapping("/items/{productId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeProduct(@PathVariable @NotBlank String userId, @PathVariable @NotBlank String productId) {
		service.removeProduct(userId, productId);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeCart(@PathVariable @NotBlank String userId) {
		service.removeCart(userId);
	}

}
