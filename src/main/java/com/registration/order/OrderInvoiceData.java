package com.registration.order;

import com.registration.entity.Order;
import com.registration.entity.User;

public class OrderInvoiceData {

    private final Order order;
    private final User user;

    public OrderInvoiceData(Order order, User user) {
        this.order = order;
        this.user = user;
    }

    public Order getOrder() { return order; }
    public User getUser() { return user; }
}