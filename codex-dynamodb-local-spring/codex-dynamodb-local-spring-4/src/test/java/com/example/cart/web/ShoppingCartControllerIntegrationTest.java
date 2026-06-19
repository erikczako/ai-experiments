package com.example.cart.web;

import java.math.BigDecimal;
import java.util.UUID;

import com.example.cart.support.DynamoDbLocalTestSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartControllerIntegrationTest extends DynamoDbLocalTestSupport {

	@LocalServerPort
	private int port;

	private RestClient restClient;

	@BeforeEach
	void setUpRestClient() {
		this.restClient = RestClient.builder().baseUrl("http://localhost:" + this.port).build();
	}

	@Test
	void readsEmptyShoppingCart() {
		String userId = uniqueUserId();

		ResponseEntity<ShoppingCartResponse> response = exchange(userId, HttpMethod.GET, "/api/v1/cart", null,
				ShoppingCartResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().userId()).isEqualTo(userId);
		assertThat(response.getBody().items()).isEmpty();
		assertThat(response.getBody().totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void addsProductToShoppingCart() {
		String userId = uniqueUserId();

		ResponseEntity<CartItemResponse> addResponse = exchange(userId, HttpMethod.POST, "/api/v1/cart/items",
				new AddCartItemRequest("sku-1", new BigDecimal("12.99")), CartItemResponse.class);
		ResponseEntity<ShoppingCartResponse> cartResponse = exchange(userId, HttpMethod.GET, "/api/v1/cart", null,
				ShoppingCartResponse.class);

		assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(addResponse.getHeaders().getLocation()).hasPath("/api/v1/cart/items/sku-1");
		assertThat(addResponse.getBody()).isEqualTo(new CartItemResponse("sku-1", new BigDecimal("12.99")));
		assertThat(cartResponse.getBody()).isNotNull();
		assertThat(cartResponse.getBody().items())
			.containsExactly(new CartItemResponse("sku-1", new BigDecimal("12.99")));
		assertThat(cartResponse.getBody().totalPrice()).isEqualByComparingTo("12.99");
	}

	@Test
	void rejectsDuplicateProductForSameUser() {
		String userId = uniqueUserId();
		AddCartItemRequest request = new AddCartItemRequest("sku-duplicate", new BigDecimal("10.00"));

		exchange(userId, HttpMethod.POST, "/api/v1/cart/items", request, CartItemResponse.class);
		ResponseEntity<ProblemDetail> duplicateResponse = exchange(userId, HttpMethod.POST, "/api/v1/cart/items",
				request, ProblemDetail.class);

		assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(duplicateResponse.getBody()).isNotNull();
		assertThat(duplicateResponse.getBody().getDetail()).contains("already in the shopping cart");
	}

	@Test
	void allowsSameProductForDifferentUsers() {
		AddCartItemRequest request = new AddCartItemRequest("sku-shared", new BigDecimal("10.00"));

		ResponseEntity<CartItemResponse> firstResponse = exchange(uniqueUserId(), HttpMethod.POST, "/api/v1/cart/items",
				request, CartItemResponse.class);
		ResponseEntity<CartItemResponse> secondResponse = exchange(uniqueUserId(), HttpMethod.POST,
				"/api/v1/cart/items", request, CartItemResponse.class);

		assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

	@Test
	void rejectsUnitPriceLessThanOrEqualToZero() {
		ResponseEntity<ProblemDetail> zeroPriceResponse = exchange(uniqueUserId(), HttpMethod.POST,
				"/api/v1/cart/items", new AddCartItemRequest("sku-free", BigDecimal.ZERO), ProblemDetail.class);
		ResponseEntity<ProblemDetail> negativePriceResponse = exchange(uniqueUserId(), HttpMethod.POST,
				"/api/v1/cart/items", new AddCartItemRequest("sku-negative", new BigDecimal("-1.00")),
				ProblemDetail.class);

		assertThat(zeroPriceResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(negativePriceResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void removesProductFromShoppingCart() {
		String userId = uniqueUserId();
		exchange(userId, HttpMethod.POST, "/api/v1/cart/items",
				new AddCartItemRequest("sku-remove", new BigDecimal("5.00")), CartItemResponse.class);
		exchange(userId, HttpMethod.POST, "/api/v1/cart/items",
				new AddCartItemRequest("sku-keep", new BigDecimal("7.00")), CartItemResponse.class);

		ResponseEntity<Void> deleteResponse = exchange(userId, HttpMethod.DELETE, "/api/v1/cart/items/sku-remove", null,
				Void.class);
		ResponseEntity<ShoppingCartResponse> cartResponse = exchange(userId, HttpMethod.GET, "/api/v1/cart", null,
				ShoppingCartResponse.class);

		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(cartResponse.getBody()).isNotNull();
		assertThat(cartResponse.getBody().items())
			.containsExactly(new CartItemResponse("sku-keep", new BigDecimal("7.00")));
	}

	@Test
	void removesWholeShoppingCart() {
		String userId = uniqueUserId();
		exchange(userId, HttpMethod.POST, "/api/v1/cart/items", new AddCartItemRequest("sku-a", new BigDecimal("1.00")),
				CartItemResponse.class);
		exchange(userId, HttpMethod.POST, "/api/v1/cart/items", new AddCartItemRequest("sku-b", new BigDecimal("2.00")),
				CartItemResponse.class);

		ResponseEntity<Void> deleteResponse = exchange(userId, HttpMethod.DELETE, "/api/v1/cart", null, Void.class);
		ResponseEntity<ShoppingCartResponse> cartResponse = exchange(userId, HttpMethod.GET, "/api/v1/cart", null,
				ShoppingCartResponse.class);

		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(cartResponse.getBody()).isNotNull();
		assertThat(cartResponse.getBody().items()).isEmpty();
		assertThat(cartResponse.getBody().totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void requiresUserContextHeader() {
		ResponseEntity<ProblemDetail> response = exchangeWithoutUser(HttpMethod.GET, "/api/v1/cart", null,
				ProblemDetail.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	private <T> ResponseEntity<T> exchange(String userId, HttpMethod method, String url, Object body,
			Class<T> responseType) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-User-Id", userId);
		return exchange(method, url, new HttpEntity<>(body, headers), responseType);
	}

	private <T> ResponseEntity<T> exchangeWithoutUser(HttpMethod method, String url, Object body,
			Class<T> responseType) {
		return exchange(method, url, new HttpEntity<>(body), responseType);
	}

	private <T> ResponseEntity<T> exchange(HttpMethod method, String url, HttpEntity<?> entity, Class<T> responseType) {
		RestClient.RequestBodySpec request = this.restClient.method(method).uri(url);
		request.headers((headers) -> headers.addAll(entity.getHeaders()));
		if (entity.getBody() != null) {
			request.body(entity.getBody());
		}
		return request.retrieve().onStatus(HttpStatusCode::isError, (request1, response) -> {
		}).toEntity(responseType);
	}

	private String uniqueUserId() {
		return "user-" + UUID.randomUUID();
	}

}
