package com.example.cart.config;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("local")
@Configuration(proxyBeanMethods = false)
class DynamoDbLocalConfiguration {

	@Bean(initMethod = "start", destroyMethod = "stop")
	DynamoDBProxyServer dynamoDbLocalServer(DynamoDbProperties properties) throws Exception {
		URI endpoint = properties.endpoint();
		if (endpoint == null || endpoint.getPort() < 1) {
			throw new IllegalStateException("The local profile requires cart.dynamodb.endpoint with an explicit port.");
		}
		Path nativeLibraries = Path.of("target", "native-libs").toAbsolutePath();
		if (!Files.isDirectory(nativeLibraries)) {
			throw new IllegalStateException("DynamoDBLocal native libraries were not found at " + nativeLibraries
					+ ". Run Maven so process-resources can copy them.");
		}
		System.setProperty("sqlite4java.library.path", nativeLibraries.toString());
		return ServerRunner.createServerFromCommandLineArgs(
				new String[] { "-inMemory", "-port", Integer.toString(endpoint.getPort()) });
	}

}
