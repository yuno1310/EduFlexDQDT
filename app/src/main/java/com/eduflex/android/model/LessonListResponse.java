package com.eduflex.android.model;

import java.util.List;

public class LessonListResponse {
    private boolean success;
    private String message;
    private List<Lesson> listLesson;

    public LessonListResponse(boolean success, String message, List<Lesson> listLesson) {
        this.success = success;
        this.message = message;
        this.listLesson = listLesson;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Lesson> getListLesson() {
        return listLesson;
    }

    public void setListLesson(List<Lesson> listLesson) {
        this.listLesson = listLesson;
    }
}
