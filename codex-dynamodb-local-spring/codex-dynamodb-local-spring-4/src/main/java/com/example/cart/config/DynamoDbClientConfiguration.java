package com.example.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

@Configuration(proxyBeanMethods = false)
class DynamoDbClientConfiguration {

	@Bean
	DynamoDbClient dynamoDbClient(DynamoDbProperties properties) {
		DynamoDbClientBuilder builder = DynamoDbClient.builder()
			.region(Region.of(properties.region()))
			.credentialsProvider(credentialsProvider(properties));
		if (properties.endpoint() != null) {
			builder.endpointOverride(properties.endpoint());
		}
		return builder.build();
	}

	private AwsCredentialsProvider credentialsProvider(DynamoDbProperties properties) {
		if (properties.endpoint() == null) {
			return DefaultCredentialsProvider.create();
		}
		return StaticCredentialsProvider.create(AwsBasicCredentials.create("local", "local"));
	}

}
