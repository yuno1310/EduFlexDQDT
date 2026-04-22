package com.eduflex.android.model;

public class Course {
    private String courseID;
    private String title;
    private String learningMode;
    private String status;
    private String description;
    private String imageUrl;
    private Long price;

    public Course(String courseID, String title, String learningMode, String status) {
        this.courseID = courseID;
        this.title = title;
        this.learningMode = learningMode;
        this.status = status;
    }

    public String getCourseID() { return courseID; }
    public void setCourseID(String courseID) { this.courseID = courseID; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLearningMode() { return learningMode; }
    public void setLearningMode(String learningMode) { this.learningMode = learningMode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
}
