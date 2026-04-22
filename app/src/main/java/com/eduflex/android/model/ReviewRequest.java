package com.eduflex.android.model;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {
    @SerializedName("courseId")
    private String courseId;
    @SerializedName("userId")
    private String userId;
    @SerializedName("rating")
    private int rating;
    @SerializedName("comment")
    private String comment;

    public ReviewRequest(String courseId, String userId, int rating, String comment) {
        this.courseId = courseId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
    }
}
