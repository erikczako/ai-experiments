package com.example.cart;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.example.cart.web.CartResponse;
import com.example.cart.web.ProductResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ShoppingCartIntegrationTest {

	private static final int DYNAMODB_PORT = availablePort();

	private static final DynamoDBProxyServer SERVER = startDynamoDbLocal();

	@LocalServerPort
	private int applicationPort;

	@AfterAll
	static void stopDynamoDbLocal() throws Exception {
		SERVER.stop();
	}

	@DynamicPropertySource
	static void dynamoDbProperties(DynamicPropertyRegistry registry) {
		registry.add("cart.dynamodb.endpoint", () -> "http://localhost:" + DYNAMODB_PORT);
	}

	@Test
	void exercisesCartLifecycleAndConstraints() {
		var client = authenticatedClient();

		assertThat(client.get().uri("/api/cart").retrieve().body(CartResponse.class).products()).isEmpty();

		var addResponse = client.post()
			.uri("/api/cart/products")
			.body(Map.of("productId", "sku-1", "unitPrice", 12.50))
			.retrieve()
			.toEntity(ProductResponse.class);
		assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(addResponse.getBody().productId()).isEqualTo("sku-1");

		assertThat(
				statusOf(client.post().uri("/api/cart/products").body(Map.of("productId", "sku-1", "unitPrice", 13))))
			.isEqualTo(HttpStatus.CONFLICT);
		assertThat(statusOf(client.post().uri("/api/cart/products").body(Map.of("productId", "sku-2", "unitPrice", 0))))
			.isEqualTo(HttpStatus.BAD_REQUEST);

		var cart = client.get().uri("/api/cart").retrieve().body(CartResponse.class);
		assertThat(cart.userId()).isEqualTo("test-user");
		assertThat(cart.products()).containsExactly(new ProductResponse("sku-1", new java.math.BigDecimal("12.5")));

		client.delete().uri("/api/cart/products/sku-1").retrieve().toBodilessEntity();
		assertThat(client.get().uri("/api/cart").retrieve().body(CartResponse.class).products()).isEmpty();

		client.post()
			.uri("/api/cart/products")
			.body(Map.of("productId", "sku-2", "unitPrice", 5))
			.retrieve()
			.toBodilessEntity();
		client.delete().uri("/api/cart").retrieve().toBodilessEntity();
		assertThat(client.get().uri("/api/cart").retrieve().body(CartResponse.class).products()).isEmpty();
	}

	@Test
	void rejectsUnauthenticatedRequests() {
		var client = RestClient.create("http://localhost:" + applicationPort);
		assertThat(statusOf(client.get().uri("/api/cart"))).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	private RestClient authenticatedClient() {
		return RestClient.builder()
			.baseUrl("http://localhost:" + applicationPort)
			.defaultHeaders(headers -> headers.setBasicAuth("test-user", "test-password"))
			.build();
	}

	private static HttpStatus statusOf(RestClient.RequestHeadersSpec<?> request) {
		return request.exchange((ignored, response) -> HttpStatus.valueOf(response.getStatusCode().value()));
	}

	private static DynamoDBProxyServer startDynamoDbLocal() {
		try {
			var server = ServerRunner.createServerFromCommandLineArgs(new String[] { "-inMemory", "-sharedDb",
					"-disableTelemetry", "-port", Integer.toString(DYNAMODB_PORT) });
			server.start();
			return server;
		}
		catch (Exception exception) {
			throw new IllegalStateException("Could not start DynamoDBLocal", exception);
		}
	}

	private static int availablePort() {
		try (var socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
		catch (IOException exception) {
			throw new IllegalStateException("Could not allocate a local port", exception);
		}
	}

}
