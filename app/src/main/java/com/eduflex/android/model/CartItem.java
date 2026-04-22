package com.eduflex.android.model;

public class CartItem {
    private final String courseId;
    private final String courseTitle;
    private final String price;
    private final String imageUrl;

    public CartItem(String courseId, String courseTitle, String price, String imageUrl) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public String getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public String getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
}
