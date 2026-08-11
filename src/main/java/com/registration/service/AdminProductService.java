package com.registration.service;

import com.registration.dto.AdminProductRequest;
import com.registration.product.ProductResponse;
import com.registration.exception.BadRequestException;
import com.registration.exception.ConflictException;
import com.registration.exception.ResourceNotFoundException;
import com.registration.order.OrderItemRepository;
import com.registration.product.Category;
import com.registration.product.CategoryRepository;
import com.registration.product.Product;
import com.registration.product.ProductImage;
import com.registration.product.ProductRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;

    public AdminProductService(ProductRepository productRepository,
                               CategoryRepository categoryRepository,
                               OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public ProductResponse createProduct(AdminProductRequest request) {
        if (request.getCategoryId() == null) {
            throw new BadRequestException("Category ID is required");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Category not found with id: " + request.getCategoryId()));

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(category);

        Product saved = productRepository.save(product);

        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            ProductImage image = new ProductImage();
            image.setProduct(saved);
            image.setImageUrl(request.getImageUrl());
            saved.getImages().add(image);
            productRepository.save(saved);
        }

        return toProductResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Integer productId, AdminProductRequest request) {
        if (request.getCategoryId() == null) {
            throw new BadRequestException("Category ID is required");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Category not found with id: " + request.getCategoryId()));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(category);

        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            if (!product.getImages().isEmpty()) {
                product.getImages().get(0).setImageUrl(request.getImageUrl());
            } else {
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setImageUrl(request.getImageUrl());
                product.getImages().add(image);
            }
        }

        Product saved = productRepository.save(product);
        return toProductResponse(saved);
    }

    @Transactional
    public void deleteProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Explicitly check for existing order_items BEFORE deleting
        if (orderItemRepository.existsByProduct_ProductId(productId)) {
            throw new ConflictException("Cannot delete product with existing order history. Product is referenced by one or more orders.");
        }

        productRepository.delete(product);
    }

    @Transactional
    public ProductResponse addStock(Integer productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setStock(product.getStock() + quantity);
        Product saved = productRepository.save(product);
        return toProductResponse(saved);
    }

    public ProductResponse toProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setProductId(product.getProductId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setCategoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null);

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            List<String> imageUrls = product.getImages().stream()
                    .map(img -> img.getImageUrl())
                    .collect(java.util.stream.Collectors.toList());
            response.setImages(imageUrls);
        }

        return response;
    }
}
