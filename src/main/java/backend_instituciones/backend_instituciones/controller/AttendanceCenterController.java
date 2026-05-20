package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.AttendanceCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/attendance-center")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
public class AttendanceCenterController {

    private final AttendanceCenterService attendanceCenterService;

    /**
     * GET /api/v1/attendance-center/search?query=lamin&size=20
     * Search DOCENTE + ESTUDIANTE by name, email or documentNumber.
     */
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(attendanceCenterService.searchPersons(
                TenantContext.getInstitutionId(), query, size));
    }

    /**
     * GET /api/v1/attendance-center/teachers/{teacherId}
     * ?weekStart=2026-05-11&weekEnd=2026-05-17&courseId=11
     */
    @GetMapping("/teachers/{teacherId}")
    public ResponseEntity<?> getTeacherProfile(
            @PathVariable Long teacherId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd,
            @RequestParam(required = false) Long courseId) {
        return ResponseEntity.ok(attendanceCenterService.getTeacherProfile(
                TenantContext.getInstitutionId(), teacherId, weekStart, weekEnd, courseId));
    }

    /**
     * GET /api/v1/attendance-center/students/{studentId}
     * ?weekStart=2026-05-11&weekEnd=2026-05-17&courseId=11
     */
    @GetMapping("/students/{studentId}")
    public ResponseEntity<?> getStudentProfile(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd,
            @RequestParam(required = false) Long courseId) {
        return ResponseEntity.ok(attendanceCenterService.getStudentProfile(
                TenantContext.getInstitutionId(), studentId, weekStart, weekEnd, courseId));
    }
}
