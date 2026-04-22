package com.eduflex.android.model;

public class CartItem {
    private final String courseId;
    private final String courseTitle;
    private final String price;

    public CartItem(String courseId, String courseTitle, String price) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.price = price;
    }

    public String getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public String getPrice() { return price; }
}
