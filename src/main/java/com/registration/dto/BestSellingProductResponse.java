package com.registration.dto;

import java.math.BigDecimal;

public class BestSellingProductResponse {

    private Integer productId;
    private String name;
    private BigDecimal price;
    private Long totalQuantity;
    private BigDecimal totalRevenue;

    public BestSellingProductResponse() {}

    public BestSellingProductResponse(Integer productId, String name, BigDecimal price,
                                      Long totalQuantity, BigDecimal totalRevenue) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.totalQuantity = totalQuantity;
        this.totalRevenue = totalRevenue;
    }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Long getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Long totalQuantity) { this.totalQuantity = totalQuantity; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
}
