package com.eduflex.android.model;

public class SaveProgressResponse {
    private boolean success;
    private String message;
    private Double newProgressPercent;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Double getNewProgressPercent() { return newProgressPercent; }
}
