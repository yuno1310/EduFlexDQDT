package com.eduflex.android.model;

import java.util.List;

public class SubmitFillBlankResponse {
    private boolean success;
    private String message;
    private int correctCount;
    private int totalQuestions;
    private List<FillBlankResultDetail> details;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public List<FillBlankResultDetail> getDetails() {
        return details;
    }

    public static class FillBlankResultDetail {
        private long questionId;
        private boolean isCorrect;
        private String correctAnswer;

        public long getQuestionId() {
            return questionId;
        }

        public boolean isCorrect() {
            return isCorrect;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }
    }
}
