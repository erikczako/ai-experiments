package com.example.cart.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.example.cart.config.DynamoDbProperties;
import com.example.cart.domain.CartItem;
import com.example.cart.service.DuplicateProductException;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

@Repository
class DynamoDbCartRepository implements CartRepository {

	private static final String USER_ID = "userId";

	private static final String PRODUCT_ID = "productId";

	private static final String UNIT_PRICE = "unitPrice";

	private final DynamoDbClient client;

	private final String tableName;

	DynamoDbCartRepository(DynamoDbClient client, DynamoDbProperties properties) {
		this.client = client;
		this.tableName = properties.tableName();
	}

	@Override
	public List<CartItem> findByUserId(String userId) {
		List<CartItem> items = new ArrayList<>();
		Map<String, AttributeValue> lastKey = Map.of();
		do {
			QueryResponse response = client.query(query(userId, lastKey));
			response.items().stream().map(this::toCartItem).forEach(items::add);
			lastKey = response.lastEvaluatedKey();
		}
		while (!lastKey.isEmpty());
		return List.copyOf(items);
	}

	@Override
	public void add(String userId, CartItem item) {
		try {
			client.putItem(PutItemRequest.builder()
				.tableName(tableName)
				.item(item(userId, item))
				.conditionExpression("attribute_not_exists(#productId)")
				.expressionAttributeNames(Map.of("#productId", PRODUCT_ID))
				.build());
		}
		catch (ConditionalCheckFailedException ex) {
			throw new DuplicateProductException(userId, item.productId(), ex);
		}
	}

	@Override
	public void remove(String userId, String productId) {
		client.deleteItem(deleteRequest(userId, productId));
	}

	@Override
	public void removeAll(String userId) {
		findByUserId(userId).forEach(item -> client.deleteItem(deleteRequest(userId, item.productId())));
	}

	private QueryRequest query(String userId, Map<String, AttributeValue> lastKey) {
		QueryRequest.Builder builder = QueryRequest.builder()
			.tableName(tableName)
			.keyConditionExpression("#userId = :userId")
			.expressionAttributeNames(Map.of("#userId", USER_ID))
			.expressionAttributeValues(Map.of(":userId", string(userId)));
		if (!lastKey.isEmpty()) {
			builder.exclusiveStartKey(lastKey);
		}
		return builder.build();
	}

	private DeleteItemRequest deleteRequest(String userId, String productId) {
		return DeleteItemRequest.builder().tableName(tableName).key(key(userId, productId)).build();
	}

	private Map<String, AttributeValue> item(String userId, CartItem item) {
		Map<String, AttributeValue> attributes = new HashMap<>(key(userId, item.productId()));
		attributes.put(UNIT_PRICE, number(item.unitPrice()));
		return attributes;
	}

	private Map<String, AttributeValue> key(String userId, String productId) {
		return Map.of(USER_ID, string(userId), PRODUCT_ID, string(productId));
	}

	private CartItem toCartItem(Map<String, AttributeValue> item) {
		return new CartItem(item.get(PRODUCT_ID).s(), new BigDecimal(item.get(UNIT_PRICE).n()));
	}

	private AttributeValue string(String value) {
		return AttributeValue.fromS(value);
	}

	private AttributeValue number(BigDecimal value) {
		return AttributeValue.fromN(value.toPlainString());
	}

}
