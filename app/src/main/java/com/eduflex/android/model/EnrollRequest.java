package com.eduflex.android.model;

import com.google.gson.annotations.SerializedName;

public class EnrollRequest {
    @SerializedName("userId")
    private String userId;

    public EnrollRequest(String userId) {
        this.userId = userId;
    }
}
