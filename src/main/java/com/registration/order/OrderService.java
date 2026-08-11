package com.registration.order;

import com.registration.dto.CheckoutResponse;
import com.registration.dto.OrderHistoryItemResponse;
import com.registration.dto.OrderHistoryResponse;
import com.registration.dto.OrderItemResponse;
import com.registration.entity.CartItem;
import com.registration.entity.Order;
import com.registration.entity.OrderItem;
import com.registration.entity.User;
import com.registration.exception.BadRequestException;
import com.registration.product.Product;
import com.registration.product.ProductRepository;
import com.registration.repository.CartItemRepository;
import com.registration.review.ReviewRepository;
import com.registration.security.CustomAuthentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;

    public OrderService(CartItemRepository cartItemRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ReviewRepository reviewRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional

    private User getCurrentUser() {
        CustomAuthentication auth =
                (CustomAuthentication) SecurityContextHolder.getContext().getAuthentication();
        return auth.getUser();
    }

    @Transactional
    public CheckoutResponse checkout() {
        User user = getCurrentUser();

        List<CartItem> cartItems = cartItemRepository.findByUser_UserId(user.getUserId());

        if (cartItems.isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }

        List<OrderItemResponse> itemResponses = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            Integer quantity = cartItem.getQuantity();

            if (quantity > product.getStock()) {
                throw new BadRequestException(
                        "Not enough stock for product: " + product.getName()
                                + " (requested: " + quantity
                                + ", available: " + product.getStock() + ")"
                );
            }

            BigDecimal pricePerUnit = product.getPrice();
            BigDecimal totalPrice = pricePerUnit.multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(totalPrice);

            itemResponses.add(new OrderItemResponse(
                    product.getName(),
                    quantity,
                    pricePerUnit,
                    totalPrice
            ));
        }

        String orderId = UUID.randomUUID().toString();

        Order order = new Order();
        order.setOrderId(orderId);
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setStatus(Order.Status.SUCCESS);
        orderRepository.save(order);

        for (int i = 0; i < cartItems.size(); i++) {
            CartItem cartItem = cartItems.get(i);
            Product product = cartItem.getProduct();
            OrderItemResponse response = itemResponses.get(i);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPricePerUnit(response.getPricePerUnit());
            orderItem.setTotalPrice(response.getTotalPrice());
            orderItemRepository.save(orderItem);

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        cartItemRepository.deleteByUser_UserId(user.getUserId());

        return new CheckoutResponse(
                orderId,
                totalAmount,
                Order.Status.SUCCESS,
                itemResponses
        );
    }

    public List<CheckoutResponse> getOrderHistory() {
        User user = getCurrentUser();
        List<Order> orders = orderRepository.findByUser_UserId(user.getUserId());

        return orders.stream()
                .map(this::toCheckoutResponse)
                .collect(Collectors.toList());
    }

    public CheckoutResponse getOrderById(String orderId) {
        User user = getCurrentUser();
        Order order = orderRepository.findByOrderId(orderId);

        if (order == null) {
            throw new com.registration.exception.ResourceNotFoundException("Order not found with id: " + orderId);
        }

        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestException("You do not have access to this order");
        }

        return toCheckoutResponse(order);
    }

    public OrderInvoiceData getOrderWithInvoice(String orderId) {
        User user = getCurrentUser();
        Order order = orderRepository.findByOrderId(orderId);

        if (order == null) {
            throw new com.registration.exception.ResourceNotFoundException("Order not found with id: " + orderId);
        }

        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestException("You do not have access to this order");
        }

        return new OrderInvoiceData(order, user);
    }

    @Transactional
    public OrderHistoryResponse getUserOrderHistory() {
        User user = getCurrentUser();
        List<Order> orders = orderRepository.findByUser_UserIdAndStatus(user.getUserId(), Order.Status.SUCCESS);

        List<OrderHistoryItemResponse> historyItems = orders.stream()
                .flatMap(order -> orderItemRepository.findByOrder_OrderId(order.getOrderId()).stream()
                        .map(orderItem -> {
                            Product product = orderItem.getProduct();

                            String imageUrl = null;
                            if (product.getImages() != null && !product.getImages().isEmpty()) {
                                imageUrl = product.getImages().get(0).getImageUrl();
                            }

                            String categoryName = null;
                            if (product.getCategory() != null) {
                                categoryName = product.getCategory().getCategoryName();
                            }

                            return new OrderHistoryItemResponse(
                                    order.getOrderId(),
                                    product.getProductId(),
                                    product.getName(),
                                    product.getDescription(),
                                    categoryName,
                                    orderItem.getQuantity(),
                                    orderItem.getPricePerUnit(),
                                    orderItem.getTotalPrice(),
                                    imageUrl,
                                    order.getStatus().name(),
                                    order.getCreatedAt(),
                                    orderItem.getRating(),
                                    orderItem.getReview(),
                                    reviewRepository.existsByOrderIdAndProduct_ProductId(
                                            order.getOrderId(), product.getProductId())
                            );
                        }))
                .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                .collect(Collectors.toList());

        return new OrderHistoryResponse(user.getUserName(), user.getRole().name(), new OrderHistoryResponse.Orders(historyItems));
    }

    private CheckoutResponse toCheckoutResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());

        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPricePerUnit(),
                        item.getTotalPrice()
                ))
                .collect(Collectors.toList());

        return new CheckoutResponse(
                order.getOrderId(),
                order.getTotalAmount(),
                order.getStatus(),
                itemResponses
        );
    }

    @Transactional
    public void rateOrderItem(String orderId, Integer productId, Integer rating, String review) {
        User user = getCurrentUser();
        Order order = orderRepository.findByOrderId(orderId);

        if (order == null) {
            throw new com.registration.exception.ResourceNotFoundException("Order not found with id: " + orderId);
        }

        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestException("You do not have access to this order");
        }

        if (order.getStatus() != Order.Status.SUCCESS) {
            throw new BadRequestException("You can only rate items from delivered orders");
        }

        if (rating < 1 || rating > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        OrderItem orderItem = orderItemRepository.findByOrder_OrderIdAndProduct_ProductId(orderId, productId);

        if (orderItem == null) {
            throw new com.registration.exception.ResourceNotFoundException("Order item not found for product: " + productId);
        }

        orderItem.setRating(rating);
        orderItem.setReview(review);
        orderItemRepository.save(orderItem);
    }
}
