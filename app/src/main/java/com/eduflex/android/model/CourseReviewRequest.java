package com.eduflex.android.model;

public class CourseReviewRequest {

    private final int rating;
    private final String comment;

    public CourseReviewRequest(int rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }
}
