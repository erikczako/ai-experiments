# Shopping Cart Service

Small Spring Boot REST service backed by AWS DynamoDB. The table uses `userId` as
its partition key and `productId` as its sort key. A conditional write prevents
duplicate products in a user's cart.

## Requirements

- Java 25
- No Docker is required

## Local development

Start DynamoDB Local:

```bash
./scripts/run-dynamodb-local.sh
```

In another terminal, start the service with the local profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The local profile creates the `shopping-carts` table on startup. The default
profile uses the AWS SDK default credentials provider chain and does not create
the table.

## REST API

```text
GET    /carts/{userId}
POST   /carts/{userId}/items
DELETE /carts/{userId}/items/{productId}
DELETE /carts/{userId}
```

Example request:

```bash
curl -i -X POST http://localhost:8080/carts/user-1/items \
  -H 'Content-Type: application/json' \
  -d '{"productId":"product-1","unitPrice":12.50}'
```

Adding the same `productId` twice returns `409 Conflict`. A unit price less than
or equal to zero returns `400 Bad Request`.

## Build

```bash
./mvnw verify
```

The build applies and validates Spring Java Format and fails if line coverage is
below 85%. The JaCoCo HTML report is written to `target/site/jacoco/index.html`.
