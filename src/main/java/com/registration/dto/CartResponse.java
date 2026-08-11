package com.registration.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {

    private List<CartItemResponse> items;
    private BigDecimal totalAmount;

    public CartResponse() {}

    public CartResponse(List<CartItemResponse> items, BigDecimal totalAmount) {
        this.items = items;
        this.totalAmount = totalAmount;
    }

    public List<CartItemResponse> getItems() { return items; }
    public void setItems(List<CartItemResponse> items) { this.items = items; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}