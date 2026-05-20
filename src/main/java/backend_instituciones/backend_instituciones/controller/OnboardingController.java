package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.OnboardingRequest;
import backend_instituciones.backend_instituciones.service.OnboardingService;
import backend_instituciones.backend_instituciones.service.SaaSCoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
@Slf4j
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final SaaSCoreService saaSCoreService;

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

    /**
     * Called by Backend Central on activate/suspend/expire to notify license status change.
     * Body: { "institutionId": 123, "estado": "ACTIVO|SUSPENDIDO|VENCIDO" }
     */
    @PostMapping("/licencia/notificacion")
    public ResponseEntity<Void> notificacionLicencia(@RequestBody Map<String, Object> body) {
        Object idObj = body.get("institutionId");
        if (idObj == null) return ResponseEntity.ok().build();
        try {
            Long institutionId = Long.valueOf(idObj.toString());
            String estado = body.get("estado") != null ? body.get("estado").toString() : "";
            log.info("Licencia notificacion recibida: institutionId={} estado={}", institutionId, estado);
            if ("ACTIVO".equalsIgnoreCase(estado)) {
                saaSCoreService.updateCachedStatus(institutionId, true);
            } else if ("SUSPENDIDO".equalsIgnoreCase(estado) || "VENCIDO".equalsIgnoreCase(estado)) {
                saaSCoreService.updateCachedStatus(institutionId, false);
            } else {
                saaSCoreService.invalidateCache(institutionId);
            }
        } catch (NumberFormatException e) {
            log.warn("notificacionLicencia: institutionId invalido: {}", idObj);
        }
        return ResponseEntity.ok().build();
    }
}
