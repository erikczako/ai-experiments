package com.example.shoppingcart.api;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.shoppingcart.domain.CartItem;
import com.example.shoppingcart.domain.DuplicateProductException;
import com.example.shoppingcart.domain.ShoppingCartService;

@WebMvcTest(ShoppingCartController.class)
class ShoppingCartControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ShoppingCartService service;

	@Test
	void readsCart() throws Exception {
		when(this.service.getCart("user-1")).thenReturn(List.of(new CartItem("product-1", new BigDecimal("12.50"))));

		this.mockMvc.perform(get("/carts/user-1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userId").value("user-1"))
			.andExpect(jsonPath("$.products[0].productId").value("product-1"))
			.andExpect(jsonPath("$.products[0].unitPrice").value(12.50));
	}

	@Test
	void addsProduct() throws Exception {
		CartItem item = new CartItem("product-1", new BigDecimal("12.50"));
		when(this.service.addProduct("user-1", "product-1", new BigDecimal("12.50"))).thenReturn(item);

		this.mockMvc.perform(post("/carts/user-1/products").contentType(MediaType.APPLICATION_JSON).content("""
				{"productId":"product-1","unitPrice":12.50}
				""")).andExpect(status().isCreated()).andExpect(jsonPath("$.productId").value("product-1"));
	}

	@Test
	void rejectsNonPositivePrice() throws Exception {
		this.mockMvc.perform(post("/carts/user-1/products").contentType(MediaType.APPLICATION_JSON).content("""
				{"productId":"product-1","unitPrice":0}
				"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value("Request validation failed"))
			.andExpect(jsonPath("$.errors.unitPrice").exists());
	}

	@Test
	void rejectsDuplicateProduct() throws Exception {
		doThrow(new DuplicateProductException("product-1")).when(this.service)
			.addProduct("user-1", "product-1", new BigDecimal("12.50"));

		this.mockMvc.perform(post("/carts/user-1/products").contentType(MediaType.APPLICATION_JSON).content("""
				{"productId":"product-1","unitPrice":12.50}
				"""))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.detail").value("Product 'product-1' is already in the shopping cart"))
			.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void removesProductAndCart() throws Exception {
		this.mockMvc.perform(delete("/carts/user-1/products/product-1")).andExpect(status().isNoContent());
		this.mockMvc.perform(delete("/carts/user-1")).andExpect(status().isNoContent());

		verify(this.service).removeProduct("user-1", "product-1");
		verify(this.service).removeCart("user-1");
	}

	@Test
	void rejectsBlankPathVariable() throws Exception {
		this.mockMvc.perform(get(URI.create("/carts/%20"))).andExpect(status().isBadRequest());
	}

}
