package com.example.cart.config;

import java.util.List;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

@Component
@ConditionalOnProperty(prefix = "cart.dynamodb", name = "create-table", havingValue = "true")
class DynamoDbTableInitializer implements SmartInitializingSingleton {

	private final DynamoDbClient dynamoDbClient;

	private final DynamoDbProperties properties;

	DynamoDbTableInitializer(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
		this.dynamoDbClient = dynamoDbClient;
		this.properties = properties;
	}

	@Override
	public void afterSingletonsInstantiated() {
		if (!tableExists()) {
			createTable();
		}
		waitForTable();
	}

	private boolean tableExists() {
		try {
			this.dynamoDbClient.describeTable(describeTableRequest());
			return true;
		}
		catch (ResourceNotFoundException ex) {
			return false;
		}
	}

	private void createTable() {
		this.dynamoDbClient.createTable(CreateTableRequest.builder()
			.tableName(this.properties.tableName())
			.attributeDefinitions(List.of(attribute("userId"), attribute("productId")))
			.keySchema(List.of(key("userId", KeyType.HASH), key("productId", KeyType.RANGE)))
			.billingMode(BillingMode.PAY_PER_REQUEST)
			.build());
	}

	private void waitForTable() {
		for (int attempt = 0; attempt < 30; attempt++) {
			if (TableStatus.ACTIVE == this.dynamoDbClient.describeTable(describeTableRequest()).table().tableStatus()) {
				return;
			}
			sleep();
		}
		throw new IllegalStateException("DynamoDB table " + this.properties.tableName() + " did not become active.");
	}

	private DescribeTableRequest describeTableRequest() {
		return DescribeTableRequest.builder().tableName(this.properties.tableName()).build();
	}

	private AttributeDefinition attribute(String name) {
		return AttributeDefinition.builder().attributeName(name).attributeType(ScalarAttributeType.S).build();
	}

	private KeySchemaElement key(String name, KeyType keyType) {
		return KeySchemaElement.builder().attributeName(name).keyType(keyType).build();
	}

	private void sleep() {
		try {
			Thread.sleep(200);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while waiting for DynamoDB table.", ex);
		}
	}

}
