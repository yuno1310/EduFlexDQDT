package com.eduflex.android.model;

public class UpdateLessonRequest {
    private String title;
    private String contentType;
    private String videoUrl;
    private String content;
    private String parentLessonId;

    public UpdateLessonRequest(String title, String contentType, String videoUrl, String content, String parentLessonId) {
        this.title = title;
        this.contentType = contentType;
        this.videoUrl = videoUrl;
        this.content = content;
        this.parentLessonId = parentLessonId;
    }

    public String getTitle() { return title; }
    public String getContentType() { return contentType; }
    public String getVideoUrl() { return videoUrl; }
    public String getContent() { return content; }
    public String getParentLessonId() { return parentLessonId; }
}
