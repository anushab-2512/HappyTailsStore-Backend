package com.registration.review;

import java.time.LocalDateTime;

public class ReviewResponse {

    private Integer id;
    private Integer productId;
    private String userName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private boolean verifiedPurchase;

    public ReviewResponse() {}

    public ReviewResponse(Integer id, Integer productId, String userName, Integer rating,
                          String comment, LocalDateTime createdAt, boolean verifiedPurchase) {
        this.id = id;
        this.productId = productId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.verifiedPurchase = verifiedPurchase;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isVerifiedPurchase() { return verifiedPurchase; }
    public void setVerifiedPurchase(boolean verifiedPurchase) { this.verifiedPurchase = verifiedPurchase; }
}
