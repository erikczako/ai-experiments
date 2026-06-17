package com.example.cart.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cart.dynamodb")
public record DynamoDbProperties(String tableName, String region, URI endpoint, boolean initializeTable) {
}
