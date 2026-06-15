package com.example.cart.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

class DynamoDbConfigurationTest {

	private final DynamoDbConfiguration configuration = new DynamoDbConfiguration();

	@Test
	void buildsDefaultAwsClientWithoutEndpointOverride() {
		try (DynamoDbClient client = configuration
			.dynamoDbClient(new DynamoDbProperties("shopping-carts", Region.EU_CENTRAL_1, null, false))) {
			assertThat(client.serviceClientConfiguration().region()).isEqualTo(Region.EU_CENTRAL_1);
			assertThat(client.serviceClientConfiguration().endpointOverride()).isEmpty();
		}
	}

	@Test
	void buildsLocalClientWithEndpointOverride() {
		URI endpoint = URI.create("http://localhost:8000");

		try (DynamoDbClient client = configuration
			.dynamoDbClient(new DynamoDbProperties("shopping-carts", Region.US_EAST_1, endpoint, true))) {
			assertThat(client.serviceClientConfiguration().endpointOverride()).contains(endpoint);
		}
	}

}
