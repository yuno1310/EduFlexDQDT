package com.eduflex.android.model;

import com.google.gson.annotations.SerializedName;

public class QuestProgressRequest {

    @SerializedName("questType")
    private final String questType;

    @SerializedName("increment")
    private final int increment;

    public QuestProgressRequest(String questType, int increment) {
        this.questType = questType;
        this.increment = increment;
    }
}
