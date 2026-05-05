package com.eduflex.android.model;

import java.util.List;

public class QuizGetResponse {
    private boolean success;
    private String message;
    private List<QuestionResponse> questions;
    private String parentLessonId;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<QuestionResponse> getQuestions() { return questions; }
    public String getParentLessonId() { return parentLessonId; }

    public static class QuestionResponse {
        private long questionId;
        private String questionText;
        private int points;
        private List<OptionResponse> options;

        public long getQuestionId() { return questionId; }
        public String getQuestionText() { return questionText; }
        public int getPoints() { return points; }
        public List<OptionResponse> getOptions() { return options; }
    }

    public static class OptionResponse {
        private long optionId;
        private String optionText;

        public long getOptionId() { return optionId; }
        public String getOptionText() { return optionText; }
    }
}
