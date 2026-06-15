package com.example.cart.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.cart.config.DynamoDbProperties;
import com.example.cart.domain.CartItem;
import com.example.cart.service.DuplicateProductException;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

@ExtendWith(MockitoExtension.class)
class DynamoDbCartRepositoryTest {

	@Mock
	private DynamoDbClient client;

	private DynamoDbCartRepository repository;

	@BeforeEach
	void setUp() {
		repository = new DynamoDbCartRepository(client,
				new DynamoDbProperties("shopping-carts", Region.US_EAST_1, null, false));
	}

	@Test
	void readsAllQueryPagesAndMapsItems() {
		Map<String, AttributeValue> lastKey = Map.of("userId", AttributeValue.fromS("user-1"), "productId",
				AttributeValue.fromS("product-1"));
		when(client.query(any(QueryRequest.class))).thenReturn(
				QueryResponse.builder().items(item("product-1", "12.50")).lastEvaluatedKey(lastKey).build(),
				QueryResponse.builder().items(item("product-2", "3.00")).build());

		List<CartItem> items = repository.findByUserId("user-1");

		assertThat(items).containsExactly(new CartItem("product-1", new BigDecimal("12.50")),
				new CartItem("product-2", new BigDecimal("3.00")));
		ArgumentCaptor<QueryRequest> requests = ArgumentCaptor.forClass(QueryRequest.class);
		verify(client, times(2)).query(requests.capture());
		assertThat(requests.getAllValues().get(0).exclusiveStartKey()).isEmpty();
		assertThat(requests.getAllValues().get(1).exclusiveStartKey()).isEqualTo(lastKey);
	}

	@Test
	void addsItemWithAtomicDuplicateProtection() {
		repository.add("user-1", new CartItem("product-1", new BigDecimal("12.50")));

		ArgumentCaptor<PutItemRequest> request = ArgumentCaptor.forClass(PutItemRequest.class);
		verify(client).putItem(request.capture());
		assertThat(request.getValue().conditionExpression()).isEqualTo("attribute_not_exists(#productId)");
		assertThat(request.getValue().item().get("unitPrice").n()).isEqualTo("12.50");
	}

	@Test
	void mapsConditionalWriteFailureToDuplicateProduct() {
		doThrow(ConditionalCheckFailedException.builder().message("duplicate").build()).when(client)
			.putItem(any(PutItemRequest.class));

		assertThatThrownBy(() -> repository.add("user-1", new CartItem("product-1", BigDecimal.ONE)))
			.isInstanceOf(DuplicateProductException.class)
			.hasMessageContaining("product-1")
			.hasMessageContaining("user-1");
	}

	@Test
	void removesOneItemOrAnEntireCart() {
		repository.remove("user-1", "product-1");

		when(client.query(any(QueryRequest.class)))
			.thenReturn(QueryResponse.builder().items(item("product-1", "1.00"), item("product-2", "2.00")).build());
		repository.removeAll("user-1");

		ArgumentCaptor<DeleteItemRequest> requests = ArgumentCaptor.forClass(DeleteItemRequest.class);
		verify(client, times(3)).deleteItem(requests.capture());
		assertThat(requests.getAllValues()).allMatch(request -> request.tableName().equals("shopping-carts"));
	}

	private Map<String, AttributeValue> item(String productId, String unitPrice) {
		return Map.of("userId", AttributeValue.fromS("user-1"), "productId", AttributeValue.fromS(productId),
				"unitPrice", AttributeValue.fromN(unitPrice));
	}

}
