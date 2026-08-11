package com.registration.order;

import com.registration.dto.CheckoutResponse;
import com.registration.dto.OrderHistoryResponse;
import com.registration.entity.Order;
import com.registration.entity.User;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final InvoiceService invoiceService;

    public OrderController(OrderService orderService, InvoiceService invoiceService) {
        this.orderService = orderService;
        this.invoiceService = invoiceService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout() {
        CheckoutResponse response = orderService.checkout();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CheckoutResponse>> getOrderHistory() {
        List<CheckoutResponse> orders = orderService.getOrderHistory();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<CheckoutResponse> getOrderById(@PathVariable String orderId) {
        CheckoutResponse response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<OrderHistoryResponse> getUserOrderHistory() {
        OrderHistoryResponse history = orderService.getUserOrderHistory();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{orderId}/invoice")
    public ResponseEntity<ByteArrayResource> downloadInvoice(@PathVariable String orderId) {
        var result = orderService.getOrderWithInvoice(orderId);
        Order order = result.getOrder();
        User user = result.getUser();

        byte[] pdfBytes = invoiceService.generateInvoice(order, user);

        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + orderId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(resource);
    }

    @PutMapping("/{orderId}/items/{productId}/rate")
    public ResponseEntity<Map<String, String>> rateOrderItem(
            @PathVariable String orderId,
            @PathVariable Integer productId,
            @RequestBody Map<String, Object> body) {
        Integer rating = (Integer) body.get("rating");
        String review = (String) body.get("review");
        orderService.rateOrderItem(orderId, productId, rating, review);
        return ResponseEntity.ok(Map.of("message", "Rating submitted successfully"));
    }
}