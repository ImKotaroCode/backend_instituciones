package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.ProfileUpdateRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok(profileService.getProfile(TenantContext.getUserId()));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(TenantContext.getUserId(), request));
    }

    @PostMapping(value = "/photo", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadPhoto(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(profileService.uploadPhoto(TenantContext.getUserId(), file));
    }

    @DeleteMapping("/photo")
    public ResponseEntity<Void> deletePhoto() {
        profileService.deletePhoto(TenantContext.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        profileService.changePassword(TenantContext.getUserId(), body.get("currentPassword"), body.get("newPassword"));
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/complete-onboarding")
    public ResponseEntity<?> completeOnboarding() {
        return ResponseEntity.ok(profileService.completeOnboarding(TenantContext.getUserId()));
    }
}
