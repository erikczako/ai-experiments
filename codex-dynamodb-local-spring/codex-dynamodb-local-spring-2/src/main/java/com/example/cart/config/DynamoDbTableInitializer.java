package com.example.cart.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

@Component
class DynamoDbTableInitializer implements ApplicationRunner {

	private final DynamoDbClient client;

	private final DynamoDbProperties properties;

	DynamoDbTableInitializer(DynamoDbClient client, DynamoDbProperties properties) {
		this.client = client;
		this.properties = properties;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!properties.createTable() || tableExists()) {
			return;
		}
		client.createTable(CreateTableRequest.builder()
			.tableName(properties.tableName())
			.billingMode(BillingMode.PAY_PER_REQUEST)
			.keySchema(key("userId", KeyType.HASH), key("productId", KeyType.RANGE))
			.attributeDefinitions(attribute("userId"), attribute("productId"))
			.build());
		client.waiter().waitUntilTableExists(DescribeTableRequest.builder().tableName(properties.tableName()).build());
	}

	private boolean tableExists() {
		try {
			client.describeTable(DescribeTableRequest.builder().tableName(properties.tableName()).build());
			return true;
		}
		catch (ResourceNotFoundException ex) {
			return false;
		}
	}

	private AttributeDefinition attribute(String name) {
		return AttributeDefinition.builder().attributeName(name).attributeType(ScalarAttributeType.S).build();
	}

	private KeySchemaElement key(String name, KeyType type) {
		return KeySchemaElement.builder().attributeName(name).keyType(type).build();
	}

}
