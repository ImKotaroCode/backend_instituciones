package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.CourseAttendanceOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class CourseAttendanceController {

    private final CourseAttendanceOverviewService overviewService;

    @GetMapping("/api/v1/teachers/{teacherId}/attendance/today-windows")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ADMINISTRACION')")
    public ResponseEntity<?> todayWindows(
            @PathVariable Long teacherId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate resolvedDate = date != null ? date : LocalDate.now(java.time.ZoneId.of("America/Lima"));
        return ResponseEntity.ok(overviewService.getTodayWindows(
                TenantContext.getInstitutionId(), teacherId, resolvedDate));
    }
}
