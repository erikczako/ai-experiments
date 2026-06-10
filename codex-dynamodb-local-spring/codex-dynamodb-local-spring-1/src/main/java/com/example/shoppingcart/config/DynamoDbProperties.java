package com.example.shoppingcart.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("shopping-cart.dynamodb")
public record DynamoDbProperties(URI endpoint, String region, String accessKey, String secretKey, String tableName,
		boolean createTable) {
}
