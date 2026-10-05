package com.eduflex.android.api;

import android.content.Context;

import com.eduflex.android.BuildConfig;
import com.eduflex.android.auth.SessionManager;
import com.eduflex.android.auth.TokenManager;
import com.eduflex.android.model.RefreshTokenRequest;
import com.eduflex.android.model.RefreshTokenResponse;

import okhttp3.OkHttpClient;
import okhttp3.Cache;
import java.io.File;
import java.util.concurrent.TimeUnit;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = BuildConfig.API_BASE_URL.endsWith("/")
            ? BuildConfig.API_BASE_URL
            : BuildConfig.API_BASE_URL + "/";

    private static Retrofit retrofit;
    private static Retrofit authenticatedRetrofit;
    private static TokenManager tokenManager;
    private static Context appContext;
    private static Cache httpCache;
    private static TokenRefreshCoordinator refreshCoordinator;
    private static final ThreadLocal<Boolean> REFRESH_NETWORK_FAILURE =
            ThreadLocal.withInitial(() -> false);

    /**
     * Initialise with application context so the auth interceptor can read the
     * stored JWT.
     */
    public static void init(Context context) {
        appContext = context.getApplicationContext();
        tokenManager = new TokenManager(appContext);
        refreshCoordinator = new TokenRefreshCoordinator(
                new TokenRefreshCoordinator.TokenStore() {
                    public String accessToken() { return tokenManager.getToken(); }
                    public String refreshToken() { return tokenManager.getRefreshToken(); }
                    public void saveAccessToken(String token) { tokenManager.saveToken(token); }
                },
                refreshToken -> {
                    retrofit2.Response<RefreshTokenResponse> response = createService(AuthApi.class)
                            .refresh(new RefreshTokenRequest(refreshToken)).execute();
                    RefreshTokenResponse body = response.body();
                    return response.isSuccessful() && body != null && body.isSuccess()
                            ? body.getAccessToken() : null;
                });
        httpCache = new Cache(new File(appContext.getCacheDir(), "http_cache"), 10L * 1024L * 1024L);
    }

    /** Returns a Retrofit instance without auth headers (for login/register). */
    public static Retrofit getInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.HTTP_LOGGING_ENABLED
                    ? HttpLoggingInterceptor.Level.BASIC
                    : HttpLoggingInterceptor.Level.NONE);

            OkHttpClient client = new OkHttpClient.Builder()
                    .cache(httpCache)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .callTimeout(45, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
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
            logging.setLevel(BuildConfig.HTTP_LOGGING_ENABLED
                    ? HttpLoggingInterceptor.Level.BASIC
                    : HttpLoggingInterceptor.Level.NONE);

            OkHttpClient client = new OkHttpClient.Builder()
                    .cache(httpCache)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .callTimeout(45, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .authenticator((route, response) -> refreshRequest(response))
                    .addInterceptor(chain -> {
                        Request.Builder builder = chain.request().newBuilder();
                        if (tokenManager != null) {
                            String token = tokenManager.getToken();
                            if (token != null) {
                                builder.addHeader("Authorization", "Bearer " + token);
                            }
                            String userId = tokenManager.getUserId();
                            if (userId != null) {
                                builder.addHeader("X-User-Id", userId);
                            }
                        }
                        return chain.proceed(builder.build());
                    })
                    .addInterceptor(chain -> {
                        Response response = chain.proceed(chain.request());
                        boolean refreshNetworkFailed = REFRESH_NETWORK_FAILURE.get();
                        REFRESH_NETWORK_FAILURE.remove();
                        if (response.code() == 401 && appContext != null && !refreshNetworkFailed) {
                            SessionManager.forceLogout(appContext, "Session expired. Please log in again.");
                        }
                        return response;
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

    private static int responseCount(Response response) {
        int count = 1;
        while ((response = response.priorResponse()) != null) count++;
        return count;
    }

    private static Request refreshRequest(Response response) {
        if (responseCount(response) >= 2 || refreshCoordinator == null) return null;
        String authorization = response.request().header("Authorization");
        String sentToken = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring("Bearer ".length()) : null;
        TokenRefreshCoordinator.Result result = refreshCoordinator.refresh(sentToken);
        if (result.networkFailure()) REFRESH_NETWORK_FAILURE.set(true);
        return result.accessToken() == null ? null : response.request().newBuilder()
                .header("Authorization", "Bearer " + result.accessToken()).build();
    }
}
