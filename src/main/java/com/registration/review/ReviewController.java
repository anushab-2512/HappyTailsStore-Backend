package com.registration.review;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse created = reviewService.submitReview(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ReviewPageResponse> getReviews(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getReviews(productId, page, size));
    }

    @GetMapping("/products/{productId}/rating-summary")
    public ResponseEntity<RatingSummaryResponse> getRatingSummary(@PathVariable Integer productId) {
        return ResponseEntity.ok(reviewService.getRatingSummary(productId));
    }
}
