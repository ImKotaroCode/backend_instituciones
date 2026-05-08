package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.OnboardingRequest;
import backend_instituciones.backend_instituciones.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    /**
     * Called by Backend Central after creating an institution.
     * Secured by X-API-KEY header (not JWT — permitAll in SecurityConfig).
     *
     * POST /api/internal/onboarding
     * X-API-KEY: <service-token>
     * Body: { institution_id, name, admin_email }
     */
    @PostMapping("/onboarding")
    public ResponseEntity<Map<String, Object>> onboard(
            @RequestHeader("X-API-KEY") String apiKey,
            @Valid @RequestBody OnboardingRequest request) {

        Map<String, Object> result = onboardingService.onboard(apiKey, request);
        return ResponseEntity.ok(result);
    }
}
