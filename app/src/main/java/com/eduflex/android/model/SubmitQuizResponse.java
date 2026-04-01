package com.eduflex.android.model;

public class SubmitQuizResponse {
    private boolean passed;
    private String message;
    private int correctCount;
    private int totalQuestions;
    private double scorePercent;
    private Double courseProgress;
    private boolean isCourseCompleted;
    private int xpRewarded;

    public boolean isPassed() {
        return passed;
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

    public double getScorePercent() {
        return scorePercent;
    }

    public Double getCourseProgress() {
        return courseProgress;
    }

    public boolean isCourseCompleted() {
        return isCourseCompleted;
    }

    public int getXpRewarded() {
        return xpRewarded;
    }
}
