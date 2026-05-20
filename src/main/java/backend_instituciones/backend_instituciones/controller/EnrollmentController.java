package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.AcademicStatusRequest;
import backend_instituciones.backend_instituciones.dto.request.EnrollmentRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
public class EnrollmentController {

    private final EnrollmentService service;

    @GetMapping("/api/v1/enrollments")
    public ResponseEntity<?> list(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.list(TenantContext.getInstitutionId(), page, size));
    }

    @GetMapping("/api/v1/enrollments/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id, TenantContext.getInstitutionId()));
    }

    @PostMapping("/api/v1/enrollments")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> create(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(TenantContext.getInstitutionId(), request));
    }

    @PutMapping("/api/v1/enrollments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.ok(service.update(id, TenantContext.getInstitutionId(), request));
    }

    @DeleteMapping("/api/v1/enrollments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id, TenantContext.getInstitutionId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/students/{studentId}/academic-history")
    public ResponseEntity<?> academicHistory(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.getAcademicHistory(studentId, TenantContext.getInstitutionId()));
    }

    @PostMapping("/api/v1/students/{studentId}/academic-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'DOCENTE','ADMINISTRACION')")
    public ResponseEntity<?> setStatus(@PathVariable Long studentId,
                                       @Valid @RequestBody AcademicStatusRequest request) {
        return ResponseEntity.ok(service.setAcademicStatus(studentId, TenantContext.getInstitutionId(), request));
    }

    @PatchMapping("/api/v1/students/{studentId}/promote")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> promote(@PathVariable Long studentId,
                                     @RequestBody Map<String, Object> body) {
        Long yearId = Long.valueOf(body.get("academicYearId").toString());
        String obs = (String) body.getOrDefault("observation", "Promovido");
        return ResponseEntity.ok(service.quickStatus(studentId, TenantContext.getInstitutionId(), "PROMOTED", yearId, obs));
    }

    @PatchMapping("/api/v1/students/{studentId}/repeat")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> repeat(@PathVariable Long studentId,
                                    @RequestBody Map<String, Object> body) {
        Long yearId = Long.valueOf(body.get("academicYearId").toString());
        String obs = (String) body.getOrDefault("observation", "Repitente");
        return ResponseEntity.ok(service.quickStatus(studentId, TenantContext.getInstitutionId(), "REPEATING", yearId, obs));
    }

    @PatchMapping("/api/v1/students/{studentId}/withdraw")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> withdraw(@PathVariable Long studentId,
                                      @RequestBody Map<String, Object> body) {
        Long yearId = Long.valueOf(body.get("academicYearId").toString());
        String obs = (String) body.getOrDefault("observation", "Retirado");
        return ResponseEntity.ok(service.quickStatus(studentId, TenantContext.getInstitutionId(), "WITHDRAWN", yearId, obs));
    }

    @PatchMapping("/api/v1/students/{studentId}/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> transfer(@PathVariable Long studentId,
                                      @RequestBody Map<String, Object> body) {
        Long yearId = Long.valueOf(body.get("academicYearId").toString());
        String obs = (String) body.getOrDefault("observation", "Trasladado");
        return ResponseEntity.ok(service.quickStatus(studentId, TenantContext.getInstitutionId(), "TRANSFERRED", yearId, obs));
    }

    @GetMapping("/api/v1/academic-years/{id}/promotion-preview")
    public ResponseEntity<?> promotionPreview(@PathVariable Long id) {
        return ResponseEntity.ok(service.promotionPreview(id, TenantContext.getInstitutionId()));
    }

    @PostMapping("/api/v1/academic-years/{id}/promotion-execute")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> promotionExecute(@PathVariable Long id) {
        return ResponseEntity.ok(service.promotionExecute(id, TenantContext.getInstitutionId()));
    }
}
