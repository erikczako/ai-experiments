package com.example.cart.repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.example.cart.config.DynamoDbProperties;
import com.example.cart.domain.CartItem;
import com.example.cart.domain.ShoppingCart;

import org.springframework.stereotype.Repository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

@Repository
class DynamoDbShoppingCartRepository implements ShoppingCartRepository {

	private static final String USER_ID = "userId";

	private static final String PRODUCT_ID = "productId";

	private static final String UNIT_PRICE = "unitPrice";

	private final DynamoDbClient dynamoDbClient;

	private final DynamoDbProperties properties;

	DynamoDbShoppingCartRepository(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
		this.dynamoDbClient = dynamoDbClient;
		this.properties = properties;
	}

	@Override
	public ShoppingCart findByUserId(String userId) {
		QueryRequest request = QueryRequest.builder()
			.tableName(this.properties.tableName())
			.keyConditionExpression("#userId = :userId")
			.expressionAttributeNames(Map.of("#userId", USER_ID))
			.expressionAttributeValues(Map.of(":userId", AttributeValue.fromS(userId)))
			.build();

		return new ShoppingCart(userId,
				this.dynamoDbClient.queryPaginator(request).items().stream().map(this::toCartItem).toList());
	}

	@Override
	public CartItem addItem(String userId, CartItem item) {
		try {
			this.dynamoDbClient.putItem(PutItemRequest.builder()
				.tableName(this.properties.tableName())
				.item(toDynamoItem(userId, item))
				.conditionExpression("attribute_not_exists(#userId) AND attribute_not_exists(#productId)")
				.expressionAttributeNames(Map.of("#userId", USER_ID, "#productId", PRODUCT_ID))
				.build());
			return item;
		}
		catch (ConditionalCheckFailedException ex) {
			throw new DuplicateProductException(item.productId());
		}
	}

	@Override
	public void removeItem(String userId, String productId) {
		this.dynamoDbClient.deleteItem(
				DeleteItemRequest.builder().tableName(this.properties.tableName()).key(key(userId, productId)).build());
	}

	@Override
	public void removeCart(String userId) {
		findByUserId(userId).items().forEach((item) -> removeItem(userId, item.productId()));
	}

	private Map<String, AttributeValue> toDynamoItem(String userId, CartItem item) {
		Map<String, AttributeValue> values = new HashMap<>();
		values.put(USER_ID, AttributeValue.fromS(userId));
		values.put(PRODUCT_ID, AttributeValue.fromS(item.productId()));
		values.put(UNIT_PRICE, AttributeValue.fromN(item.unitPrice().toPlainString()));
		return values;
	}

	private Map<String, AttributeValue> key(String userId, String productId) {
		return Map.of(USER_ID, AttributeValue.fromS(userId), PRODUCT_ID, AttributeValue.fromS(productId));
	}

	private CartItem toCartItem(Map<String, AttributeValue> values) {
		return new CartItem(values.get(PRODUCT_ID).s(), new BigDecimal(values.get(UNIT_PRICE).n()));
	}

}
