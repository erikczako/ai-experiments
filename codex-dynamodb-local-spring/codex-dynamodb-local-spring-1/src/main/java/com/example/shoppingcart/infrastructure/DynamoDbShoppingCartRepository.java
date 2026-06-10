package com.example.shoppingcart.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import com.example.shoppingcart.config.DynamoDbProperties;
import com.example.shoppingcart.domain.CartItem;
import com.example.shoppingcart.domain.DuplicateProductException;
import com.example.shoppingcart.domain.ShoppingCartRepository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

@Repository
public class DynamoDbShoppingCartRepository implements ShoppingCartRepository {

	static final String USER_ID = "userId";

	static final String PRODUCT_ID = "productId";

	static final String UNIT_PRICE = "unitPrice";

	private final DynamoDbClient dynamoDb;

	private final DynamoDbProperties properties;

	public DynamoDbShoppingCartRepository(DynamoDbClient dynamoDb, DynamoDbProperties properties) {
		this.dynamoDb = dynamoDb;
		this.properties = properties;
	}

	@PostConstruct
	void createTable() {
		if (!properties.createTable()) {
			return;
		}
		try {
			dynamoDb.createTable(CreateTableRequest.builder()
				.tableName(properties.tableName())
				.billingMode(BillingMode.PAY_PER_REQUEST)
				.attributeDefinitions(
						AttributeDefinition.builder()
							.attributeName(USER_ID)
							.attributeType(ScalarAttributeType.S)
							.build(),
						AttributeDefinition.builder()
							.attributeName(PRODUCT_ID)
							.attributeType(ScalarAttributeType.S)
							.build())
				.keySchema(KeySchemaElement.builder().attributeName(USER_ID).keyType(KeyType.HASH).build(),
						KeySchemaElement.builder().attributeName(PRODUCT_ID).keyType(KeyType.RANGE).build())
				.build());
		}
		catch (ResourceInUseException ignored) {
			// The table already exists.
		}
	}

	@Override
	public List<CartItem> findByUserId(String userId) {
		var response = dynamoDb.query(QueryRequest.builder()
			.tableName(properties.tableName())
			.keyConditionExpression("#userId = :userId")
			.expressionAttributeNames(Map.of("#userId", USER_ID))
			.expressionAttributeValues(Map.of(":userId", stringValue(userId)))
			.build());
		return response.items()
			.stream()
			.map(item -> new CartItem(item.get(PRODUCT_ID).s(), new java.math.BigDecimal(item.get(UNIT_PRICE).n())))
			.toList();
	}

	@Override
	public void add(String userId, CartItem item) {
		try {
			dynamoDb.putItem(PutItemRequest.builder()
				.tableName(properties.tableName())
				.item(Map.of(USER_ID, stringValue(userId), PRODUCT_ID, stringValue(item.productId()), UNIT_PRICE,
						numberValue(item.unitPrice().toPlainString())))
				.conditionExpression("attribute_not_exists(#productId)")
				.expressionAttributeNames(Map.of("#productId", PRODUCT_ID))
				.build());
		}
		catch (ConditionalCheckFailedException ex) {
			throw new DuplicateProductException(item.productId());
		}
	}

	@Override
	public void removeProduct(String userId, String productId) {
		dynamoDb.deleteItem(
				DeleteItemRequest.builder().tableName(properties.tableName()).key(key(userId, productId)).build());
	}

	@Override
	public void removeCart(String userId) {
		List<WriteRequest> deletes = new ArrayList<>();
		for (CartItem item : findByUserId(userId)) {
			deletes.add(WriteRequest.builder()
				.deleteRequest(DeleteRequest.builder().key(key(userId, item.productId())).build())
				.build());
		}
		for (int start = 0; start < deletes.size(); start += 25) {
			int end = Math.min(start + 25, deletes.size());
			dynamoDb.batchWriteItem(BatchWriteItemRequest.builder()
				.requestItems(Map.of(properties.tableName(), deletes.subList(start, end)))
				.build());
		}
	}

	private static Map<String, AttributeValue> key(String userId, String productId) {
		return Map.of(USER_ID, stringValue(userId), PRODUCT_ID, stringValue(productId));
	}

	private static AttributeValue stringValue(String value) {
		return AttributeValue.builder().s(value).build();
	}

	private static AttributeValue numberValue(String value) {
		return AttributeValue.builder().n(value).build();
	}

}
