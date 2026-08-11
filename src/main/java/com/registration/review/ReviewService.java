package com.registration.review;

import com.registration.entity.Order;
import com.registration.entity.OrderItem;
import com.registration.entity.User;
import com.registration.exception.BadRequestException;
import com.registration.exception.ConflictException;
import com.registration.exception.ResourceNotFoundException;
import com.registration.order.OrderItemRepository;
import com.registration.order.OrderRepository;
import com.registration.product.Product;
import com.registration.product.ProductRepository;
import com.registration.repository.UserRepository;
import com.registration.security.CustomAuthentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private static final int MAX_COMMENT_LENGTH = 1000;

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         OrderRepository orderRepository,
                         OrderItemRepository orderItemRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        CustomAuthentication auth =
                (CustomAuthentication) SecurityContextHolder.getContext().getAuthentication();
        return auth.getUser();
    }

    @Transactional
    public ReviewResponse submitReview(CreateReviewRequest request) {
        User user = getCurrentUser();
        Integer productId = request.getProductId();
        Integer rating = request.getRating();

        if (rating == null || rating < 1 || rating > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        String orderId = resolveVerifiedOrder(user, productId, request.getOrderId());

        if (reviewRepository.existsByOrderIdAndProduct_ProductId(orderId, productId)) {
            throw new ConflictException("You have already reviewed this product for this order");
        }

        String comment = sanitizeComment(request.getComment());

        Review review = new Review();
        review.setProduct(productRepository.getReferenceById(productId));
        review.setUser(userRepository.getReferenceById(user.getUserId()));
        review.setOrderId(orderId);
        review.setRating(rating);
        review.setComment(comment);
        reviewRepository.save(review);

        // Keep the order item's rating/review in sync so Order History stays consistent.
        OrderItem orderItem = orderItemRepository.findByOrder_OrderIdAndProduct_ProductId(orderId, productId);
        if (orderItem != null) {
            orderItem.setRating(rating);
            orderItem.setReview(comment);
            orderItemRepository.save(orderItem);
        }

        return toResponse(review);
    }

    private String resolveVerifiedOrder(User user, Integer productId, String requestedOrderId) {
        if (requestedOrderId != null && !requestedOrderId.isBlank()) {
            Order order = orderRepository.findByOrderId(requestedOrderId.trim());
            if (order == null) {
                throw new ResourceNotFoundException("Order not found with id: " + requestedOrderId);
            }
            if (!order.getUser().getUserId().equals(user.getUserId())) {
                throw new BadRequestException("You do not have access to this order");
            }
            if (order.getStatus() != Order.Status.SUCCESS) {
                throw new BadRequestException("You can only review items from successful orders");
            }
            if (orderItemRepository.findByOrder_OrderIdAndProduct_ProductId(requestedOrderId.trim(), productId) == null) {
                throw new BadRequestException("This product is not part of the given order");
            }
            return requestedOrderId.trim();
        }

        List<OrderItem> purchased = orderItemRepository.findPurchasedOrderItems(
                productId, user.getUserId(), Order.Status.SUCCESS);
        if (purchased.isEmpty()) {
            throw new BadRequestException("You can only review products you have actually purchased");
        }
        return purchased.get(0).getOrder().getOrderId();
    }

    private String sanitizeComment(String comment) {
        if (comment == null) return null;
        String cleaned = comment.trim();
        if (cleaned.length() > MAX_COMMENT_LENGTH) {
            cleaned = cleaned.substring(0, MAX_COMMENT_LENGTH);
        }
        cleaned = cleaned.replaceAll("<[^>]*>", "");
        cleaned = cleaned.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
        cleaned = cleaned.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    public ReviewPageResponse getReviews(Integer productId, int page, int size) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
        Page<Review> result = reviewRepository.findByProduct_ProductId(productId, pageable);

        List<ReviewResponse> content = result.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new ReviewPageResponse(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.isLast());
    }

    public RatingSummaryResponse getRatingSummary(Integer productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        List<Object[]> rows = reviewRepository.findRatingSummaryByProductId(productId);
        if (rows.isEmpty()) {
            return new RatingSummaryResponse(productId, 0.0, 0L);
        }
        Object[] row = rows.get(0);
        double avg = ((Number) row[0]).doubleValue();
        long count = ((Number) row[1]).longValue();
        double rounded = Math.round(avg * 10.0) / 10.0;

        return new RatingSummaryResponse(productId, rounded, count);
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getProduct().getProductId(),
                review.getUser().getUserName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getOrderId() != null && !review.getOrderId().isBlank()
        );
    }
}
