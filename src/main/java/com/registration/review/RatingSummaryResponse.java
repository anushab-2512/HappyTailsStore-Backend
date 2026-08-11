package com.registration.review;

public class RatingSummaryResponse {

    private Integer productId;
    private Double averageRating;
    private Long reviewCount;

    public RatingSummaryResponse() {}

    public RatingSummaryResponse(Integer productId, Double averageRating, Long reviewCount) {
        this.productId = productId;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Long getReviewCount() { return reviewCount; }
    public void setReviewCount(Long reviewCount) { this.reviewCount = reviewCount; }
}
