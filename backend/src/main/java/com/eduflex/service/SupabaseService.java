package com.eduflex.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class SupabaseService {

    private final String supabaseUrl;
    private final String supabasePublishableKey;
    private final String supabaseSecretKey;
    private final HttpClient httpClient;

    public SupabaseService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.publishable-key}") String supabasePublishableKey,
            @Value("${supabase.secret-key}") String supabaseSecretKey
    ) {
        this.supabaseUrl = trimSlash(supabaseUrl);
        this.supabasePublishableKey = supabasePublishableKey;
        this.supabaseSecretKey = supabaseSecretKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public Map<String, Object> checkConnection() {
        validateConfig();

        String url = supabaseUrl + "/auth/v1/settings";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("apikey", supabaseSecretKey)
                .header("Authorization", "Bearer " + supabaseSecretKey)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            boolean connected = status >= 200 && status < 300;

            return Map.of(
                    "connected", connected,
                    "status", status,
                    "endpoint", url
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to connect to Supabase", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to connect to Supabase", ex);
        }
    }

    private void validateConfig() {
        if (isBlank(supabaseUrl)) {
            throw new IllegalStateException("Missing SUPABASE_URL (.env)");
        }
        if (isBlank(supabasePublishableKey)) {
            throw new IllegalStateException("Missing SUPABASE_PUBLISHABLE_KEY (.env)");
        }
        if (isBlank(supabaseSecretKey)) {
            throw new IllegalStateException("Missing SUPABASE_SECRET_KEY (.env)");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
