package com.eduflex.android.model;

public class Lesson {
    private String lessonID;
    private String title;
    private String contentType;

    public Lesson(String lessonID, String title, String contentType) {
        this.lessonID = lessonID;
        this.title = title;
        this.contentType = contentType;
    }

    public Lesson(String title, String contentType) {
        this("", title, contentType);
    }

    public String getLessonID() {
        return lessonID;
    }

    public void setLessonID(String lessonID) {
        this.lessonID = lessonID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
