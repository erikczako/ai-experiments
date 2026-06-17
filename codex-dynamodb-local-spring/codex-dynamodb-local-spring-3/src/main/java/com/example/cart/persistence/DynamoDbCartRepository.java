package com.example.cart.persistence;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.example.cart.config.DynamoDbProperties;
import com.example.cart.domain.CartProduct;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

@Repository
public class DynamoDbCartRepository {

	private static final String USER_ID = "userId";

	private static final String PRODUCT_ID = "productId";

	private static final String UNIT_PRICE = "unitPrice";

	private final DynamoDbClient client;

	private final String tableName;

	public DynamoDbCartRepository(DynamoDbClient client, DynamoDbProperties properties) {
		this.client = client;
		this.tableName = properties.tableName();
	}

	public List<CartProduct> findAll(String userId) {
		var products = new ArrayList<CartProduct>();
		var request = QueryRequest.builder()
			.tableName(tableName)
			.keyConditionExpression("#userId = :userId")
			.expressionAttributeNames(Map.of("#userId", USER_ID))
			.expressionAttributeValues(Map.of(":userId", stringValue(userId)))
			.build();
		client.queryPaginator(request).items().forEach(item -> products.add(toProduct(item)));
		return List.copyOf(products);
	}

	public void add(String userId, CartProduct product) {
		var item = Map.of(USER_ID, stringValue(userId), PRODUCT_ID, stringValue(product.productId()), UNIT_PRICE,
				numberValue(product.unitPrice()));
		try {
			client.putItem(PutItemRequest.builder()
				.tableName(tableName)
				.item(item)
				.conditionExpression("attribute_not_exists(#userId) AND attribute_not_exists(#productId)")
				.expressionAttributeNames(Map.of("#userId", USER_ID, "#productId", PRODUCT_ID))
				.build());
		}
		catch (ConditionalCheckFailedException ex) {
			throw new DuplicateProductException(product.productId());
		}
	}

	public void remove(String userId, String productId) {
		client.deleteItem(DeleteItemRequest.builder().tableName(tableName).key(key(userId, productId)).build());
	}

	public void removeAll(String userId) {
		findAll(userId).forEach(product -> remove(userId, product.productId()));
	}

	private CartProduct toProduct(Map<String, AttributeValue> item) {
		return new CartProduct(item.get(PRODUCT_ID).s(), new BigDecimal(item.get(UNIT_PRICE).n()));
	}

	private Map<String, AttributeValue> key(String userId, String productId) {
		return Map.of(USER_ID, stringValue(userId), PRODUCT_ID, stringValue(productId));
	}

	private static AttributeValue stringValue(String value) {
		return AttributeValue.builder().s(value).build();
	}

	private static AttributeValue numberValue(BigDecimal value) {
		return AttributeValue.builder().n(value.toPlainString()).build();
	}

}
