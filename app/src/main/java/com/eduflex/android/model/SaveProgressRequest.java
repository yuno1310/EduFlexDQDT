package com.eduflex.android.model;

public class SaveProgressRequest {
    private String lessonId;
    private String userId;

    public SaveProgressRequest(String lessonId, String userId) {
        this.lessonId = lessonId;
        this.userId = userId;
    }

    public String getLessonId() { return lessonId; }
    public String getUserId() { return userId; }
}
