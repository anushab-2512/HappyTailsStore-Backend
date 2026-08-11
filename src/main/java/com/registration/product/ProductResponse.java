package com.registration.product;

import java.math.BigDecimal;
import java.util.List;

public class ProductResponse {

    private Integer productId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Integer categoryId;
    private String categoryName;
    private List<String> images;
    private Double averageRating = 0.0;
    private Long reviewCount = 0L;

    public ProductResponse() {}

public ProductResponse(Integer productId, String name, String description, BigDecimal price,
                            Integer stock, Integer categoryId, String categoryName, List<String> images,
                            Double averageRating, Long reviewCount) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.images = images;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Long getReviewCount() { return reviewCount; }
    public void setReviewCount(Long reviewCount) { this.reviewCount = reviewCount; }
}