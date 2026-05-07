package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.BrandingRequest;
import backend_instituciones.backend_instituciones.service.BrandingService;
import backend_instituciones.backend_instituciones.service.SupabaseStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/provider")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PROVEEDOR')")
public class ProviderController {

    private final BrandingService brandingService;
    private final SupabaseStorageService storageService;

    @GetMapping("/config/{institutionId}")
    public ResponseEntity<?> getConfig(@PathVariable Long institutionId) {
        return ResponseEntity.ok(brandingService.getConfig(institutionId));
    }

    @PutMapping("/config/{institutionId}")
    public ResponseEntity<?> updateConfig(@PathVariable Long institutionId,
                                          @Valid @RequestBody BrandingRequest request) {
        return ResponseEntity.ok(brandingService.updateConfig(institutionId, request));
    }

    @PostMapping(value = "/upload/logo/{institutionId}", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadLogo(
            @PathVariable Long institutionId,
            @RequestParam("file") MultipartFile file) {
        String url = storageService.upload(file, "logos");
        return ResponseEntity.ok(brandingService.updateLogoUrl(institutionId, url));
    }

    @PostMapping(value = "/upload/background/{institutionId}", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadBackground(
            @PathVariable Long institutionId,
            @RequestParam("file") MultipartFile file) {
        String url = storageService.upload(file, "backgrounds");
        return ResponseEntity.ok(brandingService.updateBackgroundUrl(institutionId, url));
    }
}
