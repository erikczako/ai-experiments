package com.example.cart.domain;

import java.math.BigDecimal;

public record CartProduct(String productId, BigDecimal unitPrice) {
}
