package com.example.cart.support;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class DynamoDbLocalTestSupport {

	private static final int DYNAMODB_LOCAL_PORT = findOpenPort();

	private static DynamoDBProxyServer server;

	@BeforeAll
	static void startDynamoDbLocal() {
		startServerIfNecessary();
	}

	@AfterAll
	static void stopDynamoDbLocal() throws Exception {
		if (server != null) {
			server.stop();
			server = null;
		}
	}

	@DynamicPropertySource
	static void dynamoDbProperties(DynamicPropertyRegistry registry) {
		startServerIfNecessary();
		registry.add("cart.dynamodb.region", () -> "us-east-1");
		registry.add("cart.dynamodb.endpoint", () -> "http://localhost:" + DYNAMODB_LOCAL_PORT);
		registry.add("cart.dynamodb.create-table", () -> "true");
		registry.add("cart.dynamodb.table-name", () -> "shopping-cart-test");
	}

	private static synchronized void startServerIfNecessary() {
		if (server != null) {
			return;
		}
		Path nativeLibraries = Path.of("target", "native-libs").toAbsolutePath();
		if (!Files.isDirectory(nativeLibraries)) {
			throw new IllegalStateException("DynamoDBLocal native libraries were not found at " + nativeLibraries);
		}
		try {
			System.setProperty("sqlite4java.library.path", nativeLibraries.toString());
			server = ServerRunner.createServerFromCommandLineArgs(
					new String[] { "-inMemory", "-port", Integer.toString(DYNAMODB_LOCAL_PORT) });
			server.start();
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to start DynamoDBLocal.", ex);
		}
	}

	private static int findOpenPort() {
		try (ServerSocket serverSocket = new ServerSocket(0)) {
			return serverSocket.getLocalPort();
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to find an open port for DynamoDBLocal.", ex);
		}
	}

}
