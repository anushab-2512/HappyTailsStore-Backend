package com.registration.dto;

import com.registration.entity.Order;
import java.math.BigDecimal;
import java.util.List;

public class CheckoutResponse {

    private String orderId;
    private BigDecimal totalAmount;
    private Order.Status status;
    private List<OrderItemResponse> items;

    public CheckoutResponse() {}

    public CheckoutResponse(String orderId, BigDecimal totalAmount, Order.Status status,
                            List<OrderItemResponse> items) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.items = items;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Order.Status getStatus() { return status; }
    public void setStatus(Order.Status status) { this.status = status; }

    public List<OrderItemResponse> getItems() { return items; }
    public void setItems(List<OrderItemResponse> items) { this.items = items; }
}
