package com.eduflex.android.model;

import com.google.gson.annotations.SerializedName;

public class DailyQuestResponse {

    @SerializedName("questId")
    private long questId;

    private String title;
    private String description;

    @SerializedName("questType")
    private String questType;

    @SerializedName("targetCount")
    private int targetCount;

    @SerializedName("xpReward")
    private int xpReward;

    @SerializedName("currentCount")
    private int currentCount;

    private boolean completed;

    public long getQuestId() { return questId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getQuestType() { return questType; }
    public int getTargetCount() { return targetCount; }
    public int getXpReward() { return xpReward; }
    public int getCurrentCount() { return currentCount; }
    public boolean isCompleted() { return completed; }
}
