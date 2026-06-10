package com.example.shoppingcart.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.example.shoppingcart.config.DynamoDbProperties;
import com.example.shoppingcart.domain.CartItem;
import com.example.shoppingcart.domain.DuplicateProductException;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;

class DynamoDbShoppingCartRepositoryTests {

	private final DynamoDbClient dynamoDb = mock(DynamoDbClient.class);

	private final DynamoDbProperties properties = properties(true);

	private final DynamoDbShoppingCartRepository repository = new DynamoDbShoppingCartRepository(this.dynamoDb,
			this.properties);

	@Test
	void createsTableWhenEnabledAndIgnoresExistingTable() {
		this.repository.createTable();
		doThrow(ResourceInUseException.builder().message("exists").build()).when(this.dynamoDb)
			.createTable(any(CreateTableRequest.class));
		this.repository.createTable();

		ArgumentCaptor<CreateTableRequest> request = ArgumentCaptor.forClass(CreateTableRequest.class);
		verify(this.dynamoDb, times(2)).createTable(request.capture());
		assertThat(request.getValue().tableName()).isEqualTo("shopping-cart");
		assertThat(request.getValue().keySchema()).hasSize(2);
	}

	@Test
	void skipsTableCreationWhenDisabled() {
		new DynamoDbShoppingCartRepository(this.dynamoDb, properties(false)).createTable();

		verify(this.dynamoDb, never()).createTable(any(CreateTableRequest.class));
	}

	@Test
	void findsCartItems() {
		when(this.dynamoDb.query(any(QueryRequest.class)))
			.thenReturn(QueryResponse.builder().items(item("user-1", "product-1", "12.50")).build());

		assertThat(this.repository.findByUserId("user-1"))
			.containsExactly(new CartItem("product-1", new BigDecimal("12.50")));
	}

	@Test
	void addsProductWithDuplicateProtection() {
		this.repository.add("user-1", new CartItem("product-1", new BigDecimal("12.50")));

		ArgumentCaptor<PutItemRequest> request = ArgumentCaptor.forClass(PutItemRequest.class);
		verify(this.dynamoDb).putItem(request.capture());
		assertThat(request.getValue().conditionExpression()).contains("attribute_not_exists");
		assertThat(request.getValue().item().get("unitPrice").n()).isEqualTo("12.50");
	}

	@Test
	void reportsDuplicateProduct() {
		doThrow(ConditionalCheckFailedException.builder().message("duplicate").build()).when(this.dynamoDb)
			.putItem(any(PutItemRequest.class));

		assertThatThrownBy(() -> this.repository.add("user-1", new CartItem("product-1", new BigDecimal("12.50"))))
			.isInstanceOf(DuplicateProductException.class)
			.hasMessageContaining("product-1");
	}

	@Test
	void removesProduct() {
		this.repository.removeProduct("user-1", "product-1");

		ArgumentCaptor<DeleteItemRequest> request = ArgumentCaptor.forClass(DeleteItemRequest.class);
		verify(this.dynamoDb).deleteItem(request.capture());
		assertThat(request.getValue().key()).containsKeys("userId", "productId");
	}

	@Test
	void removesWholeCartInDynamoDbBatchSizes() {
		List<Map<String, AttributeValue>> items = new ArrayList<>();
		for (int index = 0; index < 26; index++) {
			items.add(item("user-1", "product-" + index, "1.00"));
		}
		when(this.dynamoDb.query(any(QueryRequest.class))).thenReturn(QueryResponse.builder().items(items).build());

		this.repository.removeCart("user-1");

		ArgumentCaptor<BatchWriteItemRequest> request = ArgumentCaptor.forClass(BatchWriteItemRequest.class);
		verify(this.dynamoDb, times(2)).batchWriteItem(request.capture());
		assertThat(request.getAllValues().get(0).requestItems().get("shopping-cart")).hasSize(25);
		assertThat(request.getAllValues().get(1).requestItems().get("shopping-cart")).hasSize(1);
	}

	@Test
	void removingEmptyCartDoesNotWriteBatch() {
		when(this.dynamoDb.query(any(QueryRequest.class))).thenReturn(QueryResponse.builder().items(List.of()).build());

		this.repository.removeCart("user-1");

		verify(this.dynamoDb, never()).batchWriteItem(any(BatchWriteItemRequest.class));
	}

	private static DynamoDbProperties properties(boolean createTable) {
		return new DynamoDbProperties(URI.create("http://localhost:8000"), "eu-central-1", "local", "local",
				"shopping-cart", createTable);
	}

	private static Map<String, AttributeValue> item(String userId, String productId, String unitPrice) {
		return Map.of("userId", AttributeValue.builder().s(userId).build(), "productId",
				AttributeValue.builder().s(productId).build(), "unitPrice",
				AttributeValue.builder().n(unitPrice).build());
	}

}
