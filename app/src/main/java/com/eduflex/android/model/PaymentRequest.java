package com.eduflex.android.model;

public class PaymentRequest {
    private String userId;
    private String paymentMethod;

    public PaymentRequest(String userId, String paymentMethod) {
        this.userId = userId;
        this.paymentMethod = paymentMethod;
    }

    public String getUserId() {
        return userId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
}
