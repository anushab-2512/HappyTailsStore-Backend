package com.registration.dto;

public class CreatePaymentOrderResponse {

    private String razorpayOrderId;
    private Integer amountInPaise;
    private String currency;
    private String keyId;

    public CreatePaymentOrderResponse() {}

    public CreatePaymentOrderResponse(String razorpayOrderId, Integer amountInPaise, String currency, String keyId) {
        this.razorpayOrderId = razorpayOrderId;
        this.amountInPaise = amountInPaise;
        this.currency = currency;
        this.keyId = keyId;
    }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public Integer getAmountInPaise() { return amountInPaise; }
    public void setAmountInPaise(Integer amountInPaise) { this.amountInPaise = amountInPaise; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }
}