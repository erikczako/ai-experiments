package com.example.shoppingcart.config;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDbConfiguration {

	@Bean
	DynamoDbClient dynamoDbClient(DynamoDbProperties properties) {
		var builder = DynamoDbClient.builder()
			.region(Region.of(properties.region()))
			.credentialsProvider(StaticCredentialsProvider
				.create(AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())));
		URI endpoint = properties.endpoint();
		if (endpoint != null) {
			builder.endpointOverride(endpoint);
		}
		return builder.build();
	}

}
