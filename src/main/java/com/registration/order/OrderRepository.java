package com.registration.order;

import com.registration.entity.Order;
import com.registration.entity.Order.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByUser_UserId(Integer userId);

    Order findByOrderId(String orderId);

    List<Order> findByUser_UserIdAndStatus(Integer userId, Status status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status AND o.createdAt >= :start AND o.createdAt < :end")
    BigDecimal sumRevenueBetween(@Param("status") Status status,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.createdAt >= :start AND o.createdAt < :end")
    Long countBetween(@Param("status") Status status,
                      @Param("start") LocalDateTime start,
                      @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal sumRevenue(@Param("status") Status status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") Status status);
}