package com.example.cart.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cart.security")
public record CartSecurityProperties(String username, String password) {
}
