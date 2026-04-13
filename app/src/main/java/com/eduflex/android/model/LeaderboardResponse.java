package com.eduflex.android.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LeaderboardResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("leaderBoard")
    private List<LeaderboardItem> leaderBoard;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<LeaderboardItem> getLeaderBoard() { return leaderBoard; }

    public static class LeaderboardItem {
        @SerializedName("rank")
        private int rank;

        @SerializedName("userID")
        private String userId;

        @SerializedName("fullName")
        private String fullName;

        @SerializedName("xp")
        private int xp;

        @SerializedName("level")
        private int level;

        public int getRank() { return rank; }
        public String getUserId() { return userId; }
        public String getFullName() { return fullName; }
        public int getXp() { return xp; }
        public int getLevel() { return level; }
    }
}
