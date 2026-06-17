package com.example.cart.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

@Configuration
class DynamoDbConfiguration {

	@Bean
	DynamoDbClient dynamoDbClient(DynamoDbProperties properties) {
		var builder = DynamoDbClient.builder().region(Region.of(properties.region()));
		if (properties.endpoint() != null) {
			var localCredentials = AwsBasicCredentials.builder().accessKeyId("local").secretAccessKey("local").build();
			builder.endpointOverride(properties.endpoint())
				.credentialsProvider(StaticCredentialsProvider.create(localCredentials));
		}
		else {
			builder.credentialsProvider(DefaultCredentialsProvider.builder().build());
		}
		return builder.build();
	}

	@Bean
	Runnable initializeDynamoDbTable(DynamoDbClient client, DynamoDbProperties properties) {
		if (properties.initializeTable()) {
			try {
				client.createTable(CreateTableRequest.builder()
					.tableName(properties.tableName())
					.billingMode(BillingMode.PAY_PER_REQUEST)
					.attributeDefinitions(List.of(
							AttributeDefinition.builder()
								.attributeName("userId")
								.attributeType(ScalarAttributeType.S)
								.build(),
							AttributeDefinition.builder()
								.attributeName("productId")
								.attributeType(ScalarAttributeType.S)
								.build()))
					.keySchema(List.of(KeySchemaElement.builder().attributeName("userId").keyType(KeyType.HASH).build(),
							KeySchemaElement.builder().attributeName("productId").keyType(KeyType.RANGE).build()))
					.build());
			}
			catch (ResourceInUseException ignored) {
				// The table already exists.
			}
		}
		return () -> {
		};
	}

}
