package com.example.cart.config;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import static org.assertj.core.api.Assertions.assertThat;

class DynamoDbClientConfigurationTest {

	@Test
	void createsClientWithoutEndpointOverride() {
		DynamoDbClient client = new DynamoDbClientConfiguration()
			.dynamoDbClient(new DynamoDbProperties("shopping-cart", "us-east-1", null, false));

		assertThat(client).isNotNull();
		client.close();
	}

}
