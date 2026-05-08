package backend_instituciones.backend_instituciones.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class SaaSCoreService {

    private final RestClient restClient;

    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private static final int FAILURE_THRESHOLD = 5;
    private static final long OPEN_DURATION_MS = 30_000;

    private final ConcurrentHashMap<String, Boolean> institutionCache = new ConcurrentHashMap<>();

    public SaaSCoreService(
            @Value("${saas.core.url}") String baseUrl,
            @Value("${saas.core.service-token}") String serviceToken) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + serviceToken)
                .build();
    }

    public boolean isInstitutionActive(Long institutionId) {
        if (isCircuitOpen()) {
            log.warn("Circuit open — using cached institution status for {}", institutionId);
            return institutionCache.getOrDefault(institutionId.toString(), true);
        }

        try {
            var response = restClient.get()
                    .uri("/api/institutions/{id}", institutionId)
                    .retrieve()
                    .body(Map.class);

            boolean active = response != null && Boolean.TRUE.equals(response.get("active"));
            institutionCache.put(institutionId.toString(), active);
            failureCount.set(0);
            return active;
        } catch (Exception e) {
            log.error("SaaS Core unreachable: {}", e.getMessage());
            failureCount.incrementAndGet();
            lastFailureTime.set(System.currentTimeMillis());
            return institutionCache.getOrDefault(institutionId.toString(), true);
        }
    }

    /**
     * Validates an institution's apiKey against Backend Central.
     * Returns institution data (id, nombre, estado) or throws if invalid/inactive.
     */
    public LicenciaResponse validateApiKey(String apiKey) {
        try {
            LicenciaResponse response = restClient.get()
                    .uri("/api/licencia/validar/{apiKey}", apiKey)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new RuntimeException("Invalid apiKey: HTTP " + res.getStatusCode());
                    })
                    .body(LicenciaResponse.class);

            if (response == null) {
                throw new RuntimeException("Empty response from Central");
            }
            if (!"ACTIVO".equalsIgnoreCase(response.getEstado())) {
                throw new RuntimeException("Institution not active: " + response.getEstado());
            }
            return response;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Central unreachable: " + e.getMessage(), e);
        }
    }

    private boolean isCircuitOpen() {
        if (failureCount.get() >= FAILURE_THRESHOLD) {
            long elapsed = System.currentTimeMillis() - lastFailureTime.get();
            if (elapsed < OPEN_DURATION_MS) return true;
            failureCount.set(0);
        }
        return false;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LicenciaResponse {
        @JsonProperty("id")
        private Long id;
        @JsonProperty("nombre")
        private String nombre;
        @JsonProperty("estado")
        private String estado;
        @JsonProperty("apiKey")
        private String apiKey;
        @JsonProperty("backendUrl")
        private String backendUrl;
    }
}
