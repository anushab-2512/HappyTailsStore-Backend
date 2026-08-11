package com.registration.config;

import com.razorpay.RazorpayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class RazorpayConfig {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Bean
    public RazorpayClient razorpayClient() throws Exception {
        if (keyId == null || keyId.trim().isEmpty()
                || keySecret == null || keySecret.trim().isEmpty()) {
            System.out.println("WARN: Razorpay keys not configured (RAZORPAY_KEY_ID / RAZORPAY_KEY_SECRET) - payment calls will fail until they are set");
        }
        String id = (keyId == null || keyId.trim().isEmpty()) ? "not-configured" : keyId.trim();
        String secret = (keySecret == null || keySecret.trim().isEmpty()) ? "not-configured" : keySecret.trim();
        return new RazorpayClient(id, secret);
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        System.out.println("=== Razorpay Configuration Startup ===");
        System.out.println("keyId: " + keyId);
        System.out.println("keySecret: [HIDDEN]");
        System.out.println("=== Razorpay Configuration Complete ===");
    }
}