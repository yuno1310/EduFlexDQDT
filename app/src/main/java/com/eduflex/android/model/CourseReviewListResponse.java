package com.eduflex.android.model;

import java.util.List;

public class CourseReviewListResponse {

    private boolean success;
    private String message;
    private List<CourseReviewItem> reviews;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<CourseReviewItem> getReviews() {
        return reviews;
    }
}
