package com.registration.dto;

import java.util.List;

public class OrderHistoryResponse {

    private String username;
    private String role;

    private Orders orders;

    public OrderHistoryResponse() {}

    public OrderHistoryResponse(String username, String role, Orders orders) {
        this.username = username;
        this.role = role;
        this.orders = orders;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Orders getOrders() { return orders; }
    public void setOrders(Orders orders) { this.orders = orders; }

    public static class Orders {
        private List<OrderHistoryItemResponse> products;

        public Orders() {}

        public Orders(List<OrderHistoryItemResponse> products) {
            this.products = products;
        }

        public List<OrderHistoryItemResponse> getProducts() { return products; }
        public void setProducts(List<OrderHistoryItemResponse> products) { this.products = products; }
    }
}