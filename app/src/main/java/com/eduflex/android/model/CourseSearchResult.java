package com.eduflex.android.model;

import com.google.gson.annotations.SerializedName;

public class CourseSearchResult {
    @SerializedName("courseId")
    private String courseId;

    @SerializedName("title")
    private String title;

    @SerializedName("imageUrl")
    private String imageUrl;

    public String getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
}
