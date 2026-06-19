package com.example.cart.config;

import java.net.URI;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cart.dynamodb")
public record DynamoDbProperties(@NotBlank String tableName, @NotBlank String region, URI endpoint,
		boolean createTable) {

}
