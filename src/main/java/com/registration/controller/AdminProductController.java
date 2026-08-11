package com.registration.controller;

import com.registration.dto.AdminProductRequest;
import com.registration.product.ProductResponse;
import com.registration.service.AdminProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody AdminProductRequest request) {
        ProductResponse response = adminProductService.createProduct(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Integer productId,
            @Valid @RequestBody AdminProductRequest request) {
        ProductResponse response = adminProductService.updateProduct(productId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}/stock")
    public ResponseEntity<ProductResponse> addStock(
            @PathVariable Integer productId,
            @RequestBody Map<String, Integer> body) {
        Integer quantity = body.get("quantity");
        ProductResponse response = adminProductService.addStock(productId, quantity);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer productId) {
        adminProductService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}
