package com.example.shoppingcart.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;

class DynamoDbConfigurationTests {

	@Test
	void createsClientForLocalAndAwsEndpoints() {
		DynamoDbConfiguration configuration = new DynamoDbConfiguration();
		DynamoDbProperties local = new DynamoDbProperties(URI.create("http://localhost:8000"), "eu-central-1", "local",
				"local", "shopping-cart", true);
		DynamoDbProperties aws = new DynamoDbProperties(null, "eu-central-1", "key", "secret", "shopping-cart", false);

		try (var localClient = configuration.dynamoDbClient(local); var awsClient = configuration.dynamoDbClient(aws)) {
			assertThat(localClient.serviceName()).isEqualTo("dynamodb");
			assertThat(awsClient.serviceName()).isEqualTo("dynamodb");
		}
	}

}
