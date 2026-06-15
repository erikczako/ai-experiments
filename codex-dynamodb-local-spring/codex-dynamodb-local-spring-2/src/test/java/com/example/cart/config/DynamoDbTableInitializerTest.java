package com.example.cart.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

@ExtendWith(MockitoExtension.class)
class DynamoDbTableInitializerTest {

	@Mock
	private DynamoDbClient client;

	@Mock
	private ApplicationArguments arguments;

	@Mock
	private DynamoDbWaiter waiter;

	@Test
	void doesNothingWhenTableCreationIsDisabled() {
		initializer(false).run(arguments);

		verify(client, never()).describeTable(any(DescribeTableRequest.class));
		verify(client, never()).createTable(any(CreateTableRequest.class));
	}

	@Test
	void doesNothingWhenTableAlreadyExists() {
		when(client.describeTable(any(DescribeTableRequest.class))).thenReturn(null);

		initializer(true).run(arguments);

		verify(client, never()).createTable(any(CreateTableRequest.class));
	}

	@Test
	void createsMissingTableAndWaitsUntilItExists() {
		doThrow(ResourceNotFoundException.builder().message("missing").build()).when(client)
			.describeTable(any(DescribeTableRequest.class));
		when(client.waiter()).thenReturn(waiter);

		initializer(true).run(arguments);

		verify(client).createTable(any(CreateTableRequest.class));
		verify(waiter).waitUntilTableExists(any(DescribeTableRequest.class));
	}

	private DynamoDbTableInitializer initializer(boolean createTable) {
		return new DynamoDbTableInitializer(client,
				new DynamoDbProperties("shopping-carts", Region.US_EAST_1, null, createTable));
	}

}
