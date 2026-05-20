package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.AcademicPeriodConfigRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.AcademicPeriodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/academic-periods")
@RequiredArgsConstructor
public class AcademicPeriodController {

    private final AcademicPeriodService academicPeriodService;

    @GetMapping("/config")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ESTUDIANTE','PADRE','ADMINISTRACION')")
    public ResponseEntity<?> getConfig() {
        return ResponseEntity.ok(
                academicPeriodService.getConfig(TenantContext.getInstitutionId()));
    }

    @PutMapping("/config")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<?> saveConfig(@Valid @RequestBody AcademicPeriodConfigRequest request) {
        return ResponseEntity.ok(
                academicPeriodService.saveConfig(TenantContext.getInstitutionId(), request));
    }
}
