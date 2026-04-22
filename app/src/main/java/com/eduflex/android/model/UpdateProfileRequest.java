package com.eduflex.android.model;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequest {
    @SerializedName("fullName")
    private final String fullName;

    public UpdateProfileRequest(String fullName) {
        this.fullName = fullName;
    }
}
