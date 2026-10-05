package com.eduflex.android.api;

import java.io.IOException;

/** Coordinates refresh so concurrent 401 responses perform one network call. */
public final class TokenRefreshCoordinator {
    public interface TokenStore {
        String accessToken();
        String refreshToken();
        void saveAccessToken(String token);
    }

    public interface RefreshCall {
        String execute(String refreshToken) throws IOException;
    }

    public record Result(String accessToken, boolean networkFailure) {}

    private final TokenStore store;
    private final RefreshCall refreshCall;

    public TokenRefreshCoordinator(TokenStore store, RefreshCall refreshCall) {
        this.store = store;
        this.refreshCall = refreshCall;
    }

    public synchronized Result refresh(String accessTokenSent) {
        String current = store.accessToken();
        if (current != null && !current.equals(accessTokenSent)) {
            return new Result(current, false);
        }
        String refreshToken = store.refreshToken();
        if (refreshToken == null) return new Result(null, false);
        try {
            String replacement = refreshCall.execute(refreshToken);
            if (replacement != null) store.saveAccessToken(replacement);
            return new Result(replacement, false);
        } catch (IOException networkFailure) {
            return new Result(null, true);
        }
    }
}
