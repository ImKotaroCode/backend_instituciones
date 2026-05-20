package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.GuardianRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.GuardianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
public class GuardianController {

    private final GuardianService service;

    @GetMapping("/api/v1/students/{studentId}/guardians")
    public ResponseEntity<?> listGuardians(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.listGuardians(studentId, TenantContext.getInstitutionId()));
    }

    @PostMapping("/api/v1/students/{studentId}/guardians")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> addGuardian(@PathVariable Long studentId,
                                         @Valid @RequestBody GuardianRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.addGuardian(studentId, TenantContext.getInstitutionId(), request));
    }

    @DeleteMapping("/api/v1/students/{studentId}/guardians/{guardianId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<Void> removeGuardian(@PathVariable Long studentId,
                                               @PathVariable Long guardianId) {
        service.removeGuardian(studentId, guardianId, TenantContext.getInstitutionId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/guardians/{guardianId}/students")
    public ResponseEntity<?> listStudentsOfGuardian(@PathVariable Long guardianId) {
        return ResponseEntity.ok(service.listStudentsOfGuardian(guardianId, TenantContext.getInstitutionId()));
    }

    @PutMapping("/api/v1/guardians/{guardianId}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> updateRelationship(@PathVariable Long guardianId,
                                                @PathVariable Long studentId,
                                                @Valid @RequestBody GuardianRequest request) {
        return ResponseEntity.ok(service.updateRelationship(studentId, guardianId, TenantContext.getInstitutionId(), request));
    }
}
