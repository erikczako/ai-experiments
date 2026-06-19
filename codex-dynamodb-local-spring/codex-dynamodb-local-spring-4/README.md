# Shopping Cart Service

Small Spring Boot microservice that stores shopping cart items in DynamoDB. The table uses `userId` as the partition
key and `productId` as the sort key, which prevents duplicate products per user at the database level.

## API

All endpoints use `X-User-Id` as the current user context.

```http
GET /api/v1/cart
POST /api/v1/cart/items
DELETE /api/v1/cart/items/{productId}
DELETE /api/v1/cart
```

Example add item request:

```json
{
  "productId": "sku-123",
  "unitPrice": 12.99
}
```

## Local Development

Run with the `local` profile. This starts DynamoDBLocal in-process on port `8000`, creates the table if needed, and
does not require Docker.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Verification

```bash
mvn spring-javaformat:apply verify
```

The Maven build enforces Spring Java Format and a JaCoCo instruction coverage minimum of 85%.
