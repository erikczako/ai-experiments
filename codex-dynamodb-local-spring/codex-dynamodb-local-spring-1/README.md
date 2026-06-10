# Shopping Cart Service

Small Spring Boot REST microservice backed by AWS DynamoDB. For local development it
connects to DynamoDB Local on `http://localhost:8000`; Docker is not required.

Each product is stored as one DynamoDB item. `userId` is the string partition key and
`productId` is the string sort key. `unitPrice` is stored as a DynamoDB number. A
conditional put prevents duplicate product entries for the same user.

## Prerequisites

- Java 21
- Maven 3.9+
- `curl` and `tar` to download DynamoDB Local

## Run locally

Start DynamoDB Local in one terminal:

```bash
./scripts/run-dynamodb-local.sh
```

Start the service in another terminal:

```bash
mvn spring-boot:run
```

The service creates the `shopping-cart` table on startup. Configuration can be
overridden with `DYNAMODB_ENDPOINT`, `AWS_REGION`, `AWS_ACCESS_KEY_ID`,
`AWS_SECRET_ACCESS_KEY`, `SHOPPING_CART_TABLE`, and `CREATE_DYNAMODB_TABLE`.

## API

```bash
# Read a cart
curl http://localhost:8080/carts/user-1

# Add a product
curl -i -X POST http://localhost:8080/carts/user-1/products \
  -H 'Content-Type: application/json' \
  -d '{"productId":"product-1","unitPrice":12.50}'

# Remove a product
curl -i -X DELETE http://localhost:8080/carts/user-1/products/product-1

# Remove the whole cart
curl -i -X DELETE http://localhost:8080/carts/user-1
```

Adding the same product twice returns `409 Conflict`. Blank identifiers and a
`unitPrice` less than or equal to zero return `400 Bad Request`.

## Quality checks

```bash
mvn spring-javaformat:apply
mvn verify
```

`mvn verify` validates Spring Java Format and fails when line coverage is below 85%.
The JaCoCo report is generated at `target/site/jacoco/index.html`.
