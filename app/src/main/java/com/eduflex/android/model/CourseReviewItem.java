package com.eduflex.android.model;

public class CourseReviewItem {

    private String userId;
    private String reviewerName;
    private int rating;
    private String comment;
    private String createdAt;

    public String getUserId() {
        return userId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
