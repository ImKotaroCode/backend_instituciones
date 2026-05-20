package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.AnnouncementSeenRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    // ── Admin endpoints ───────────────────────────────────────────────────────

    /**
     * GET /api/v1/announcements?monthKey=2026-05
     * Admin list — all statuses.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<?> list(
            @RequestParam(required = false) String monthKey) {
        return ResponseEntity.ok(
                announcementService.list(TenantContext.getInstitutionId(), monthKey));
    }

    /**
     * GET /api/v1/announcements/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(
                announcementService.get(id, TenantContext.getInstitutionId()));
    }

    /**
     * POST /api/v1/announcements
     * multipart/form-data — file optional.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<?> create(
            @RequestParam(required = false, defaultValue = "ANUNCIO") String kind,
            @RequestParam(required = false, defaultValue = "") String title,
            @RequestParam(required = false) String linkUrl,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String monthKey,
            @RequestParam(required = false) List<String> targetRoles,
            @RequestParam(name = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(
                announcementService.create(
                        TenantContext.getInstitutionId(),
                        TenantContext.getUserId(),
                        kind, title, linkUrl, priority, status, monthKey,
                        targetRoles != null ? targetRoles : List.of(),
                        file));
    }

    /**
     * PUT /api/v1/announcements/{id}
     * multipart/form-data — file optional.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestParam(required = false) String kind,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String linkUrl,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String monthKey,
            @RequestParam(required = false) List<String> targetRoles,
            @RequestParam(name = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(
                announcementService.update(
                        id, TenantContext.getInstitutionId(),
                        kind, title, linkUrl, priority, status, monthKey,
                        targetRoles != null ? targetRoles : List.of(),
                        file));
    }

    /**
     * DELETE /api/v1/announcements/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        announcementService.delete(id, TenantContext.getInstitutionId());
        return ResponseEntity.noContent().build();
    }

    // ── User endpoints ────────────────────────────────────────────────────────

    /**
     * GET /api/v1/announcements/inbox?role=ESTUDIANTE&userId=10&monthKey=2026-05
     * Anuncios PUBLICADO visibles para el rol del usuario, con seenAt.
     * Default: mes actual.
     */
    @GetMapping("/inbox")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> inbox(
            @RequestParam String role,
            @RequestParam Long userId,
            @RequestParam(required = false) String monthKey,
            @RequestParam(required = false) String kind) {
        return ResponseEntity.ok(
                announcementService.inbox(
                        TenantContext.getInstitutionId(), role, userId, monthKey, kind));
    }

    /**
     * POST /api/v1/announcements/{id}/seen
     * Marca anuncio como visto para un usuario. Idempotente.
     */
    @PostMapping("/{id}/seen")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markSeen(
            @PathVariable Long id,
            @RequestBody AnnouncementSeenRequest req) {
        return ResponseEntity.ok(
                announcementService.markSeen(id, TenantContext.getInstitutionId(), req.getUserId()));
    }
}
