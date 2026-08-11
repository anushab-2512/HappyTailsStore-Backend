package com.registration.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateQuantityRequest {

    @NotNull(message = "productId is required")
    private Integer productId;

    @NotNull(message = "quantity is required")
    @Min(value = 0, message = "quantity must be 0 or greater")
    private Integer quantity;

    public UpdateQuantityRequest() {}

    public UpdateQuantityRequest(Integer productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
