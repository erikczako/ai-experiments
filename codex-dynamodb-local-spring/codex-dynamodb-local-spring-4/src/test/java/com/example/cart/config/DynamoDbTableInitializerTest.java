package com.example.cart.config;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DynamoDbTableInitializerTest {

	@Test
	void skipsCreationWhenTableAlreadyExists() {
		DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
		when(dynamoDbClient.describeTable(any(DescribeTableRequest.class))).thenReturn(DescribeTableResponse.builder()
			.table(TableDescription.builder().tableStatus(TableStatus.ACTIVE).build())
			.build());

		new DynamoDbTableInitializer(dynamoDbClient, new DynamoDbProperties("shopping-cart", "us-east-1", null, true))
			.afterSingletonsInstantiated();

		verify(dynamoDbClient, times(2)).describeTable(any(DescribeTableRequest.class));
		verify(dynamoDbClient, never()).createTable(any(CreateTableRequest.class));
	}

}
