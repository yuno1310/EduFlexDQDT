package com.eduflex.android.model;

public class PaymentResponse {
    private boolean success;
    private String message;
    private boolean simulated;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSimulated() {
        return simulated;
    }
}
