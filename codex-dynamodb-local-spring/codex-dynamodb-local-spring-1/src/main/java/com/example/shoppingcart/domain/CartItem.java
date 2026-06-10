package com.example.shoppingcart.domain;

import java.math.BigDecimal;

public record CartItem(String productId, BigDecimal unitPrice) {
}
