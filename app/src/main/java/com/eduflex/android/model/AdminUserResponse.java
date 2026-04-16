package com.eduflex.android.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AdminUserResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("users")
    private List<AdminUser> users;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<AdminUser> getUsers() { return users; }

    public static class AdminUser {
        @SerializedName("userId")
        private String userId;

        @SerializedName("email")
        private String email;

        @SerializedName("fullName")
        private String fullName;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("xp")
        private int xp;

        @SerializedName("level")
        private int level;

        @SerializedName("streakDays")
        private int streakDays;

        @SerializedName("lastStudyDate")
        private String lastStudyDate;

        @SerializedName("role")
        private String role;

        public String getUserId() { return userId; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public String getCreatedAt() { return createdAt; }
        public int getXp() { return xp; }
        public int getLevel() { return level; }
        public int getStreakDays() { return streakDays; }
        public String getLastStudyDate() { return lastStudyDate; }
        public String getRole() { return role != null ? role : "user"; }
        public boolean isAdmin() { return "admin".equals(getRole()); }
    }
}
