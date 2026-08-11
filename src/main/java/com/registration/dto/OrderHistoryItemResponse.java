package com.registration.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderHistoryItemResponse {

    private String orderId;
    private Integer productId;
    private String name;
    private String description;
    private String categoryName;
    private Integer quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal totalPrice;
    private String imageUrl;
    private String orderStatus;
    private LocalDateTime orderDate;
    private Integer rating;
    private String review;
    private boolean reviewed;

    public OrderHistoryItemResponse() {}

    public OrderHistoryItemResponse(String orderId, Integer productId, String name, String description,
                                    String categoryName, Integer quantity, BigDecimal pricePerUnit,
                                    BigDecimal totalPrice, String imageUrl, String orderStatus,
                                    LocalDateTime orderDate, Integer rating, String review, boolean reviewed) {
        this.orderId = orderId;
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.categoryName = categoryName;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.totalPrice = totalPrice;
        this.imageUrl = imageUrl;
        this.orderStatus = orderStatus;
        this.orderDate = orderDate;
        this.rating = rating;
        this.review = review;
        this.reviewed = reviewed;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }
}