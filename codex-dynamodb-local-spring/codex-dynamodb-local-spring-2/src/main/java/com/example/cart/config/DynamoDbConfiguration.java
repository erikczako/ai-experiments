package com.example.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

@Configuration(proxyBeanMethods = false)
class DynamoDbConfiguration {

	@Bean
	DynamoDbClient dynamoDbClient(DynamoDbProperties properties) {
		DynamoDbClientBuilder builder = DynamoDbClient.builder().region(properties.region());
		if (properties.endpoint() != null) {
			builder.endpointOverride(properties.endpoint())
				.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("local", "local")));
		}
		return builder.build();
	}

}
