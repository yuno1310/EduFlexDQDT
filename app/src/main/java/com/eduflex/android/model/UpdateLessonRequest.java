package com.eduflex.android.model;

public class UpdateLessonRequest {
    private String title;
    private String contentType;
    private String videoUrl;
    private String content;

    public UpdateLessonRequest(String title, String contentType, String videoUrl, String content) {
        this.title = title;
        this.contentType = contentType;
        this.videoUrl = videoUrl;
        this.content = content;
    }

    public String getTitle() { return title; }
    public String getContentType() { return contentType; }
    public String getVideoUrl() { return videoUrl; }
    public String getContent() { return content; }
}
