package com.example.shoppingcart.api;

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

import com.example.shoppingcart.domain.CartItem;
import com.example.shoppingcart.domain.ShoppingCartService;

@Validated
@RestController
@RequestMapping("/carts")
public class ShoppingCartController {

	private final ShoppingCartService service;

	public ShoppingCartController(ShoppingCartService service) {
		this.service = service;
	}

	@GetMapping("/{userId}")
	public ShoppingCartResponse getCart(@PathVariable @NotBlank String userId) {
		return new ShoppingCartResponse(userId, service.getCart(userId));
	}

	@PostMapping("/{userId}/products")
	@ResponseStatus(HttpStatus.CREATED)
	public CartItem addProduct(@PathVariable @NotBlank String userId, @Valid @RequestBody AddProductRequest request) {
		return service.addProduct(userId, request.productId(), request.unitPrice());
	}

	@DeleteMapping("/{userId}/products/{productId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeProduct(@PathVariable @NotBlank String userId, @PathVariable @NotBlank String productId) {
		service.removeProduct(userId, productId);
	}

	@DeleteMapping("/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeCart(@PathVariable @NotBlank String userId) {
		service.removeCart(userId);
	}

}
