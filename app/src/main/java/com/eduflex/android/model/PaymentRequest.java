package com.eduflex.android.model;

public class PaymentRequest {
    private String userId;
    private String courseId;

    public PaymentRequest(String userId, String courseId) {
        this.userId = userId;
        this.courseId = courseId;
    }

    public String getUserId() {
        return userId;
    }

    public String getCourseId() {
        return courseId;
    }
}
