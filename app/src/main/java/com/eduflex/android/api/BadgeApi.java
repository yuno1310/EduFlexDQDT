package com.eduflex.android.api;

import com.eduflex.android.model.BadgeResponse;
import com.eduflex.android.model.UserBadgeResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface BadgeApi {

    @GET("/api/badges")
    Call<List<BadgeResponse>> getAllBadges();

    @GET("/api/users/{userId}/badges")
    Call<List<UserBadgeResponse>> getUserBadges(@Path("userId") String userId);
}
