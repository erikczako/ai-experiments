package com.example.cart.web;

import java.security.Principal;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.cart.service.ShoppingCartService;

@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {

	private final ShoppingCartService service;

	public ShoppingCartController(ShoppingCartService service) {
		this.service = service;
	}

	@GetMapping
	CartResponse getCart(Principal principal) {
		return CartResponse.from(principal.getName(), service.getCart(principal.getName()));
	}

	@PostMapping("/products")
	@ResponseStatus(HttpStatus.CREATED)
	ProductResponse addProduct(Principal principal, @Valid @RequestBody AddProductRequest request) {
		return ProductResponse.from(service.addProduct(principal.getName(), request.productId(), request.unitPrice()));
	}

	@DeleteMapping("/products/{productId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void removeProduct(Principal principal, @PathVariable String productId) {
		service.removeProduct(principal.getName(), productId);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void removeCart(Principal principal) {
		service.removeCart(principal.getName());
	}

}
