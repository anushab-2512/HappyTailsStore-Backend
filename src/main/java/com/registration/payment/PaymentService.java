package com.registration.payment;

import com.registration.dto.CreatePaymentOrderResponse;
import com.registration.dto.VerifyPaymentRequest;
import com.registration.exception.BadRequestException;
import com.registration.exception.UnauthorizedException;
import com.registration.order.OrderService;
import com.registration.security.CustomAuthentication;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final String razorpayKeyId;
    private final String razorpayKeySecret;

    private final RazorpayClient razorpayClient;
    private final OrderService orderService;
    private final com.registration.cart.CartService cartService;

    public PaymentService(RazorpayClient razorpayClient,
                          OrderService orderService,
                          com.registration.cart.CartService cartService,
                          @Value("${razorpay.key.id}") String razorpayKeyId,
                          @Value("${razorpay.key.secret}") String razorpayKeySecret) {
        this.razorpayClient = razorpayClient;
        this.orderService = orderService;
        this.cartService = cartService;
        this.razorpayKeyId = razorpayKeyId;
        this.razorpayKeySecret = razorpayKeySecret;
    }

    public CreatePaymentOrderResponse createRazorpayOrder() {
        Integer userId = getCurrentUserId();

        com.registration.dto.CartResponse cart = cartService.getCart();
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }

        BigDecimal totalAmount = cart.getTotalAmount();
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Cart total amount must be positive");
        }

        Integer amountInPaise = totalAmount.multiply(new BigDecimal("100")).setScale(0, java.math.RoundingMode.HALF_UP).intValue();

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "order_" + System.currentTimeMillis());
            orderRequest.put("payment_capture", 1);

            logger.info("Creating Razorpay order - keyId: {}, amountInPaise: {}", razorpayKeyId, amountInPaise);
            logger.info("Request payload: {}", orderRequest.toString());

            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);

            logger.info("Razorpay order created - orderId: {}, response: {}", razorpayOrder.get("id"), razorpayOrder.toString());

            String keyId = razorpayKeyId;
            String razorpayOrderId = razorpayOrder.get("id");

            return new CreatePaymentOrderResponse(razorpayOrderId, amountInPaise, "INR", keyId);
        } catch (Exception e) {
            logger.error("Failed to create Razorpay order: ", e);
            throw new BadRequestException("Failed to create payment order: " + e.getMessage());
        }
    }

    public com.registration.dto.CheckoutResponse verifyAndCompletePayment(VerifyPaymentRequest request) {
        Integer userId = getCurrentUserId();

        try {
            String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("razorpay_order_id", request.getRazorpayOrderId());
            jsonObject.put("razorpay_payment_id", request.getRazorpayPaymentId());
            jsonObject.put("razorpay_signature", request.getRazorpaySignature());
            
            logger.info("Verifying payment - orderId: {}, paymentId: {}", request.getRazorpayOrderId(), request.getRazorpayPaymentId());
            logger.info("Verification payload: {}", payload);
            
            boolean isVerified = Utils.verifyPaymentSignature(jsonObject, razorpayKeySecret);
            if (!isVerified) {
                logger.error("Payment verification failed - invalid signature for order: {}", request.getRazorpayOrderId());
                throw new UnauthorizedException("Payment verification failed - invalid signature");
            }
        } catch (Exception e) {
            logger.error("Payment verification failed: ", e);
            throw new UnauthorizedException("Payment verification failed: " + e.getMessage());
        }

        return orderService.checkout();
    }

    private Integer getCurrentUserId() {
        CustomAuthentication auth = (CustomAuthentication) SecurityContextHolder.getContext().getAuthentication();
        return auth.getUser().getUserId();
    }
}