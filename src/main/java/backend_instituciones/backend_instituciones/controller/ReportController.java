package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','ADMINISTRACION')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/academic-summary")
    public ResponseEntity<?> academicSummary() {
        return ResponseEntity.ok(reportService.getAcademicSummary(TenantContext.getInstitutionId()));
    }

    @GetMapping("/attendance-summary")
    public ResponseEntity<?> attendanceSummary() {
        return ResponseEntity.ok(java.util.Map.of(
                "institutionId", TenantContext.getInstitutionId(),
                "message", "Attendance summary — query attendance table by institution"
        ));
    }

    @GetMapping("/student/{id}/full")
    public ResponseEntity<?> studentFull(@PathVariable Long id) {
        return ResponseEntity.ok(java.util.Map.of("studentId", id, "grades", java.util.List.of()));
    }

    @GetMapping("/export/pdf/{type}")
    public ResponseEntity<?> exportPdf(@PathVariable String type) {
        return ResponseEntity.ok(
                reportService.requestExport(TenantContext.getInstitutionId(), TenantContext.getUserId(), type));
    }

    @GetMapping("/export/status/{jobId}")
    public ResponseEntity<?> exportStatus(@PathVariable Long jobId) {
        return ResponseEntity.ok(reportService.getJobStatus(jobId, TenantContext.getInstitutionId()));
    }
}
