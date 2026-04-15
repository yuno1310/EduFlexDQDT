package com.eduflex.android.api;

import com.eduflex.android.model.AdminUserResponse;
import com.eduflex.android.model.DeleteUserResponse;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface AdminApi {

    @GET("/api/admin/users")
    Call<AdminUserResponse> getAllUsers();

    @DELETE("/api/admin/users/{userId}")
    Call<DeleteUserResponse> deleteUser(@Path("userId") String userId);
}
