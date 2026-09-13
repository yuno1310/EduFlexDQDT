package com.eduflex.android.model;

public class AiCourseSummaryResponse {
    private String courseId;
    private String summary;
    private boolean generatedByAi;

    public String getCourseId() {
        return courseId;
    }

    public String getSummary() {
        return summary;
    }

    public boolean isGeneratedByAi() {
        return generatedByAi;
    }
}
