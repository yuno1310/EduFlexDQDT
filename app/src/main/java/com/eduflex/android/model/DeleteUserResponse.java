package com.eduflex.android.model;

import com.google.gson.annotations.SerializedName;

public class DeleteUserResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
