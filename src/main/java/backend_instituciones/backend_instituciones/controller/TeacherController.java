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
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final CourseAttendanceOverviewService courseAttendanceOverviewService;

    /**
     * GET /api/v1/teachers/{teacherId}/courses/{courseId}/attendance/overview
     * ?periodId=2&sectionId=3&dateFrom=2026-05-11&dateTo=2026-05-17
     */
    @GetMapping("/{teacherId}/courses/{courseId}/attendance/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'DOCENTE','ADMINISTRACION')")
    public ResponseEntity<?> getAttendanceOverview(
            @PathVariable Long teacherId,
            @PathVariable Long courseId,
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(courseAttendanceOverviewService.getTeacherOverview(
                TenantContext.getInstitutionId(), teacherId, courseId, periodId, sectionId, dateFrom, dateTo));
    }
}
