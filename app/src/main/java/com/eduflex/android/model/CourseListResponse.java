package com.eduflex.android.model;

import java.util.List;

public class CourseListResponse {
    private boolean success;
    private String message;
    private List<Course> listCourse;

    public CourseListResponse(boolean success, String message, List<Course> listCourse) {
        this.success = success;
        this.message = message;
        this.listCourse = listCourse;
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

    public List<Course> getListCourse() {
        return listCourse;
    }

    public void setListCourse(List<Course> listCourse) {
        this.listCourse = listCourse;
    }
}
