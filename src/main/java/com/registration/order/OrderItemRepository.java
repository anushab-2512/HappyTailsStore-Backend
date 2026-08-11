package com.registration.order;

import com.registration.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    List<OrderItem> findByOrder_OrderId(String orderId);

    boolean existsByProduct_ProductId(Integer productId);

    OrderItem findByOrder_OrderIdAndProduct_ProductId(String orderId, Integer productId);

    @Query("SELECT oi.product.productId as productId, oi.product.name as name, oi.product.price as price, SUM(oi.quantity) as totalQuantity " +
           "FROM OrderItem oi WHERE oi.order.status = :status " +
           "GROUP BY oi.product.productId, oi.product.name, oi.product.price " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProducts(@Param("status") com.registration.entity.Order.Status status);

    @Query("SELECT oi FROM OrderItem oi " +
           "WHERE oi.product.productId = :productId " +
           "AND oi.order.user.userId = :userId " +
           "AND oi.order.status = :status " +
           "ORDER BY oi.order.createdAt DESC")
    List<OrderItem> findPurchasedOrderItems(@Param("productId") Integer productId,
                                            @Param("userId") Integer userId,
                                            @Param("status") com.registration.entity.Order.Status status);
}
