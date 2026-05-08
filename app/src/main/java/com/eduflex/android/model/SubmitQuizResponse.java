package com.eduflex.android.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SubmitQuizResponse {
    private boolean passed;
    private String message;
    private int correctCount;
    private int totalQuestions;
    private double scorePercent;
    private Double courseProgress;
    private boolean isCourseCompleted;
    private int xpRewarded;

    @SerializedName("completedQuests")
    private List<CompletedQuestInfo> completedQuests;

    public boolean isPassed() { return passed; }
    public String getMessage() { return message; }
    public int getCorrectCount() { return correctCount; }
    public int getTotalQuestions() { return totalQuestions; }
    public double getScorePercent() { return scorePercent; }
    public Double getCourseProgress() { return courseProgress; }
    public boolean isCourseCompleted() { return isCourseCompleted; }
    public int getXpRewarded() { return xpRewarded; }
    public List<CompletedQuestInfo> getCompletedQuests() { return completedQuests; }

    public static class CompletedQuestInfo {
        private String title;
        private int xpReward;
        public String getTitle() { return title; }
        public int getXpReward() { return xpReward; }
    }
}
