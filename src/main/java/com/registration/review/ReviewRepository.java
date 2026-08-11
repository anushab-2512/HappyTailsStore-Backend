package com.registration.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    Optional<Review> findByOrderIdAndProduct_ProductId(String orderId, Integer productId);

    boolean existsByOrderIdAndProduct_ProductId(String orderId, Integer productId);

    Page<Review> findByProduct_ProductId(Integer productId, Pageable pageable);

    long countByProduct_ProductId(Integer productId);

    @Query("SELECT COALESCE(AVG(r.rating), 0), COUNT(r.id) FROM Review r WHERE r.product.productId = :productId")
    List<Object[]> findRatingSummaryByProductId(@Param("productId") Integer productId);

    @Query("SELECT r.product.productId, COALESCE(AVG(r.rating), 0), COUNT(r.id) FROM Review r GROUP BY r.product.productId")
    List<Object[]> findRatingSummary();
}
