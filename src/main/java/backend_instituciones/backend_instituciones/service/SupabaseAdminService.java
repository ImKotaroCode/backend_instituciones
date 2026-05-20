package backend_instituciones.backend_instituciones.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Slf4j
public class SupabaseAdminService {

    private final RestClient restClient;

    public SupabaseAdminService(
            @Value("${supabase.api-url}") String apiUrl,
            @Value("${supabase.service-key}") String serviceKey) {

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl + "/auth/v1/admin")
                .defaultHeader("Authorization", "Bearer " + serviceKey)
                .defaultHeader("apikey", serviceKey)
                .build();
    }

    /**
     * Creates a user in Supabase Auth.
     * Returns the UUID assigned by Supabase.
     */
    public String createAuthUser(String email, String password) {
        SupabaseUserResponse response = restClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "email", email,
                        "password", password,
                        "email_confirm", true
                ))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    String body = new String(res.getBody().readAllBytes());
                    log.error("Supabase Admin API error: HTTP {} — {}", res.getStatusCode(), body);
                    String bodyLower = body.toLowerCase();
                    if (bodyLower.contains("already registered") || bodyLower.contains("already been registered")
                            || bodyLower.contains("duplicate") || bodyLower.contains("already exists")) {
                        throw new RuntimeException("EMAIL_ALREADY_REGISTERED");
                    }
                    throw new RuntimeException("Supabase Admin API error: HTTP " + res.getStatusCode() + " " + body);
                })
                .body(SupabaseUserResponse.class);

        if (response == null || response.getId() == null) {
            throw new RuntimeException("Supabase returned no user ID");
        }
        return response.getId();
    }

    /**
     * Deletes a user from Supabase Auth by UUID.
     */
    public void deleteAuthUser(String supabaseUid) {
        try {
            restClient.delete()
                    .uri("/users/" + supabaseUid)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to delete Supabase Auth user {}: {}", supabaseUid, e.getMessage());
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SupabaseUserResponse {
        @JsonProperty("id")
        private String id;
        @JsonProperty("email")
        private String email;
    }
}
