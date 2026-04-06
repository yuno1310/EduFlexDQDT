package com.eduflex.android.model;

import com.google.gson.annotations.SerializedName;

public class UserBadgeResponse {

    private Long id;

    @SerializedName("userId")
    private String userId;

    @SerializedName("badgeId")
    private Long badgeId;

    @SerializedName("badgeName")
    private String badgeName;

    @SerializedName("badgeDescription")
    private String badgeDescription;

    @SerializedName("badgeIconUrl")
    private String badgeIconUrl;

    @SerializedName("earnedAt")
    private String earnedAt;

    // Getters
    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public Long getBadgeId() { return badgeId; }
    public String getBadgeName() { return badgeName; }
    public String getBadgeDescription() { return badgeDescription; }
    public String getBadgeIconUrl() { return badgeIconUrl; }
    public String getEarnedAt() { return earnedAt; }
}
