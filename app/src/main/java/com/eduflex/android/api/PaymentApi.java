package com.eduflex.android.api;

import com.eduflex.android.model.PaymentRequest;
import com.eduflex.android.model.PaymentResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PaymentApi {

    @POST("api/payment")
    Call<PaymentResponse> processPayment(@Body PaymentRequest request);
}
