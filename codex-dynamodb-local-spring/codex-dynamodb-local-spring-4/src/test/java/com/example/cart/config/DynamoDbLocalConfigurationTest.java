package com.example.cart.config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;

import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class DynamoDbLocalConfigurationTest {

	@Test
	void rejectsMissingEndpoint() {
		assertThatIllegalStateException().isThrownBy(() -> new DynamoDbLocalConfiguration()
			.dynamoDbLocalServer(new DynamoDbProperties("shopping-cart", "us-east-1", null, true)));
	}

	@Test
	void createsServerForConfiguredEndpoint() throws Exception {
		int port = findOpenPort();

		DynamoDBProxyServer server = new DynamoDbLocalConfiguration().dynamoDbLocalServer(
				new DynamoDbProperties("shopping-cart", "us-east-1", URI.create("http://localhost:" + port), true));

		assertThat(server).isNotNull();
		assertThat(System.getProperty("sqlite4java.library.path")).endsWith("target/native-libs");
	}

	private int findOpenPort() {
		try (ServerSocket serverSocket = new ServerSocket(0)) {
			return serverSocket.getLocalPort();
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to find an open port.", ex);
		}
	}

}
