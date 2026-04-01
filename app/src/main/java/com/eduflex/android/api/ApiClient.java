package com.eduflex.android.api;

import android.content.Context;

import com.eduflex.android.auth.TokenManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // TODO: Update this to your backend URL
    // Emulator: "http://10.0.2.2:8080"
    // Physical device (same WiFi): "http://<PC_IP>:8080"
    private static final String BASE_URL = "http://192.168.1.15:8080";

    private static Retrofit retrofit;
    private static Retrofit authenticatedRetrofit;
    private static TokenManager tokenManager;

    /**
     * Initialise with application context so the auth interceptor can read the
     * stored JWT.
     */
    public static void init(Context context) {
        tokenManager = new TokenManager(context.getApplicationContext());
    }

    /** Returns a Retrofit instance without auth headers (for login/register). */
    public static Retrofit getInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /**
     * Returns a Retrofit instance that attaches the JWT Bearer token to every
     * request.
     */
    public static Retrofit getAuthenticatedInstance() {
        if (authenticatedRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request.Builder builder = chain.request().newBuilder();
                        if (tokenManager != null) {
                            String token = tokenManager.getToken();
                            if (token != null) {
                                builder.addHeader("Authorization", "Bearer " + token);
                            }
                        }
                        return chain.proceed(builder.build());
                    })
                    .addInterceptor(logging)
                    .build();

            authenticatedRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return authenticatedRetrofit;
    }

    public static <T> T createService(Class<T> serviceClass) {
        return getInstance().create(serviceClass);
    }

    public static <T> T createAuthenticatedService(Class<T> serviceClass) {
        return getAuthenticatedInstance().create(serviceClass);
    }
}
