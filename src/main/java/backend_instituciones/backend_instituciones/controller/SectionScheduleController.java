package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.SectionScheduleRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.SectionScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/section-schedules")
@RequiredArgsConstructor
public class SectionScheduleController {

    private final SectionScheduleService sectionScheduleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','PROVEEDOR','DOCENTE','ESTUDIANTE','PADRE','ADMINISTRACION')")
    public ResponseEntity<?> getSchedule(@RequestParam Long sectionId) {
        return ResponseEntity.ok(
                sectionScheduleService.getSchedule(TenantContext.getInstitutionId(), sectionId));
    }

    /** GET /api/v1/section-schedules/batch?sectionIds=2,3,4 */
    @GetMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','PROVEEDOR','DOCENTE','ESTUDIANTE','PADRE','ADMINISTRACION')")
    public ResponseEntity<?> getBatch(@RequestParam List<Long> sectionIds) {
        return ResponseEntity.ok(
                sectionScheduleService.getBatch(TenantContext.getInstitutionId(), sectionIds));
    }

    @PutMapping("/{sectionId}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','PROVEEDOR', 'PADRE','ADMINISTRACION')")
    public ResponseEntity<?> updateSchedule(@PathVariable Long sectionId,
                                            @Valid @RequestBody SectionScheduleRequest request) {
        return ResponseEntity.ok(
                sectionScheduleService.updateSchedule(TenantContext.getInstitutionId(), sectionId, request));
    }
}
