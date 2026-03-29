package com.eduflex.android.model;

public class Lesson {
    private String title;
    private String contentType;

    public Lesson(String title, String contentType) {
        this.title = title;
        this.contentType = contentType;
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
