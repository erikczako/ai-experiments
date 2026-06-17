# Shopping Cart Service

Small Spring Boot REST service backed by AWS DynamoDB. A cart belongs to the
authenticated user. Each cart product is a DynamoDB item with `userId` as the
partition key and `productId` as the sort key, so duplicate products are rejected
atomically.

## Local development

Requirements: Java 25. Docker is not used.

Start DynamoDBLocal:

```bash
./mvnw test-compile dependency:copy-dependencies \
  -DincludeArtifactIds=libsqlite4java-osx,libsqlite4java-linux-amd64 \
  -DoutputDirectory=target/native-libs \
  exec:java -Dexec.mainClass=com.amazonaws.services.dynamodbv2.local.main.DynamoDBLocal \
  -Dexec.classpathScope=test -Dexec.args="-inMemory -sharedDb -disableTelemetry -port 8000" \
  -Dsqlite4java.library.path=target/native-libs
```

In another terminal, start the application:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The local-only credentials are `local-user` / `local-password`.

## API

All endpoints require HTTP Basic authentication and operate only on the authenticated
user's cart.

```text
GET    /api/cart
POST   /api/cart/products             {"productId":"sku-1","unitPrice":12.50}
DELETE /api/cart/products/{productId}
DELETE /api/cart
```

Example:

```bash
curl -u local-user:local-password http://localhost:8080/api/cart
```

For a non-local environment, configure `AWS_REGION`, `CART_API_USERNAME`, and
`CART_API_PASSWORD`. The normal AWS SDK credential provider chain is used.

## Quality checks

```bash
./mvnw verify
./mvnw spring-javaformat:apply
```

`verify` runs unit and DynamoDBLocal integration tests, checks Spring Java Format,
and fails below 85% line coverage.
