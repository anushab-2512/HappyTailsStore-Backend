package com.registration.product.services;

import com.registration.product.ProductImage;
import com.registration.product.ProductResponse;
import com.registration.product.CategoryResponse;
import com.registration.product.Product;
import com.registration.product.Category;
import com.registration.product.CategoryRepository;
import com.registration.product.ProductRepository;
import com.registration.review.RatingSummaryResponse;
import com.registration.review.ReviewRepository;
import org.springframework.stereotype.Service;
import com.registration.exception.ResourceNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        Map<Integer, RatingSummaryResponse> summary = ratingSummaryMap();
        return products.stream()
                .map(p -> toResponse(p, summary))
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return toResponse(product, ratingSummaryMap());
    }

    public List<ProductResponse> getProductsByCategory(Integer categoryId) {
        List<Product> products = productRepository.findByCategoryCategoryId(categoryId);
        Map<Integer, RatingSummaryResponse> summary = ratingSummaryMap();
        return products.stream()
                .map(p -> toResponse(p, summary))
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(cat -> new CategoryResponse(cat.getCategoryId(), cat.getCategoryName()))
                .collect(Collectors.toList());
    }

    private Map<Integer, RatingSummaryResponse> ratingSummaryMap() {
        Map<Integer, RatingSummaryResponse> map = new HashMap<>();
        for (Object[] row : reviewRepository.findRatingSummary()) {
            Integer productId = ((Number) row[0]).intValue();
            double avg = ((Number) row[1]).doubleValue();
            long count = ((Number) row[2]).longValue();
            map.put(productId, new RatingSummaryResponse(productId, round(avg), count));
        }
        return map;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private ProductResponse toResponse(Product product, Map<Integer, RatingSummaryResponse> summary) {
        List<String> imageUrls = product.getImages().stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());

        Integer categoryId = null;
        String categoryName = null;
        if (product.getCategory() != null) {
            categoryId = product.getCategory().getCategoryId();
            categoryName = product.getCategory().getCategoryName();
        }

        RatingSummaryResponse s = summary.get(product.getProductId());

        return new ProductResponse(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                categoryId,
                categoryName,
                imageUrls,
                s != null ? s.getAverageRating() : 0.0,
                s != null ? s.getReviewCount() : 0L
        );
    }
}
