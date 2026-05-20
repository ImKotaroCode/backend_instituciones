package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<?> adminDashboard(@PathVariable Long userId) {
        return ResponseEntity.ok(
                dashboardService.getAdminDashboard(TenantContext.getInstitutionId()));
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ADMINISTRACION')")
    public ResponseEntity<?> teacherDashboard(@PathVariable Long teacherId) {
        return ResponseEntity.ok(
                dashboardService.getTeacherDashboard(TenantContext.getInstitutionId(), teacherId));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ESTUDIANTE','PADRE','ADMINISTRACION')")
    public ResponseEntity<?> studentDashboard(@PathVariable Long studentId) {
        return ResponseEntity.ok(
                dashboardService.getStudentDashboard(TenantContext.getInstitutionId(), studentId));
    }
}
