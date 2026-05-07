package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.service.BrandingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicController {

    private final BrandingService brandingService;

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of(
                "alive", true,
                "timestamp", Instant.now().toString(),
                "version", "1.0.0"
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "lms", "UP",
                "database", "UP",
                "cache", "UP",
                "storage", "UP",
                "queue", "UP",
                "maintenance", false,
                "maintenanceMessage", null
        ));
    }

    @GetMapping("/institution-config/{institutionId}")
    public ResponseEntity<?> getInstitutionConfig(@PathVariable Long institutionId) {
        return ResponseEntity.ok(brandingService.getConfig(institutionId));
    }
}
