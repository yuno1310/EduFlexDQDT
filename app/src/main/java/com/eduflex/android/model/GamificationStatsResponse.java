package com.eduflex.android.model;

import com.google.gson.annotations.SerializedName;

public class GamificationStatsResponse {

    private Long id;

    @SerializedName("userId")
    private String userId;

    private int xp;
    private int level;

    @SerializedName("streakDays")
    private int streakDays;

    @SerializedName("lastStudyDate")
    private String lastStudyDate;

    // Getters
    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public int getXp() { return xp; }
    public int getLevel() { return level; }
    public int getStreakDays() { return streakDays; }
    public String getLastStudyDate() { return lastStudyDate; }
}
