package com.eduflex.android.model;

public class UpdateCourseRequest {
    private String title;
    private String learningModel;
    private String status;
    private String imageUrl;
    private Long price;
    private String description;

    public UpdateCourseRequest(String title, String learningModel, String status,
                               String imageUrl, Long price, String description) {
        this.title = title;
        this.learningModel = learningModel;
        this.status = status;
        this.imageUrl = imageUrl;
        this.price = price;
        this.description = description;
    }

    // Getters
    public String getTitle() { return title; }
    public String getLearningModel() { return learningModel; }
    public String getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }
    public Long getPrice() { return price; }
    public String getDescription() { return description; }
}
