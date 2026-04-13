package com.eduflex.android.model;

import com.google.gson.annotations.SerializedName;

public class BadgeResponse {

    private Long id;
    private String name;
    private String description;

    @SerializedName("iconUrl")
    private String iconUrl;

    @SerializedName("conditionType")
    private String conditionType;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIconUrl() { return iconUrl; }
    public String getConditionType() { return conditionType; }
}
