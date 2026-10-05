package com.eduflex.android.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class TokenRefreshCoordinatorTest {
    @Test
    public void concurrentExpiredRequestsShareOneRefresh() throws Exception {
        MemoryStore store = new MemoryStore("expired", "refresh");
        AtomicInteger calls = new AtomicInteger();
        TokenRefreshCoordinator coordinator = new TokenRefreshCoordinator(store, token -> {
            calls.incrementAndGet();
            return "replacement";
        });
        var executor = Executors.newFixedThreadPool(12);
        try {
            List<Callable<TokenRefreshCoordinator.Result>> tasks = new ArrayList<>();
            for (int i = 0; i < 20; i++) tasks.add(() -> coordinator.refresh("expired"));
            List<Future<TokenRefreshCoordinator.Result>> futures = executor.invokeAll(tasks);
            for (Future<TokenRefreshCoordinator.Result> future : futures) {
                assertEquals("replacement", future.get().accessToken());
                assertFalse(future.get().networkFailure());
            }
            assertEquals(1, calls.get());
        } finally {
            executor.shutdownNow();
        }
    }

    private static final class MemoryStore implements TokenRefreshCoordinator.TokenStore {
        private volatile String access;
        private final String refresh;

        MemoryStore(String access, String refresh) {
            this.access = access;
            this.refresh = refresh;
        }

        public String accessToken() { return access; }
        public String refreshToken() { return refresh; }
        public void saveAccessToken(String token) { access = token; }
    }
}
