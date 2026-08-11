package com.registration.payment;

import com.registration.dto.CheckoutResponse;
import com.registration.dto.CreatePaymentOrderResponse;
import com.registration.dto.VerifyPaymentRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<CreatePaymentOrderResponse> createOrder() {
        CreatePaymentOrderResponse response = paymentService.createRazorpayOrder();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<CheckoutResponse> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) {
        CheckoutResponse response = paymentService.verifyAndCompletePayment(request);
        return ResponseEntity.ok(response);
    }
}