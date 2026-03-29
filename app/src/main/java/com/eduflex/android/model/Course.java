package com.eduflex.android.model;

public class Course {
    private String courseID;
    private String title;
    private String learningMode;
    private String status;

    public Course(String courseID, String title, String learningMode, String status) {
        this.courseID = courseID;
        this.title = title;
        this.learningMode = learningMode;
        this.status = status;
    }

    public String getCourseID() {
        return courseID;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLearningMode() {
        return learningMode;
    }

    public void setLearningMode(String learningMode) {
        this.learningMode = learningMode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
