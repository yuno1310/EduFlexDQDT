package com.eduflex.android.model;

import java.util.List;

public class EnrolledCoursesResponse {
    private boolean success;
    private String message;
    private List<EnrolledCourse> enrolledCourses;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<EnrolledCourse> getEnrolledCourses() {
        return enrolledCourses;
    }
}
