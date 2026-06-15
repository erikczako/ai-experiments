package com.example.cart.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

import software.amazon.awssdk.regions.Region;

@ConfigurationProperties("app.dynamodb")
public record DynamoDbProperties(String tableName, Region region, URI endpoint, boolean createTable) {
}
