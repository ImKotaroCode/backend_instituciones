package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.domain.enums.AttendanceStatus;
import backend_instituciones.backend_instituciones.dto.request.AttendanceBulkRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/course/{courseId}/date/{date}")
    @PreAuthorize("hasAnyRole('DOCENTE','ADMIN','DIRECTOR')")
    public ResponseEntity<?> getByCourseAndDate(@PathVariable Long courseId,
                                                @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getByCourseAndDate(courseId, date));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<?> bulk(@Valid @RequestBody AttendanceBulkRequest request) {
        return ResponseEntity.ok(attendanceService.bulkRegister(
                TenantContext.getInstitutionId(), TenantContext.getUserId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Map<String, String> body) {
        AttendanceStatus status = AttendanceStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(attendanceService.update(id, TenantContext.getInstitutionId(), status));
    }

    @GetMapping("/student/{studentId}/summary")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ESTUDIANTE','PADRE')")
    public ResponseEntity<?> summary(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceService.getSummary(studentId, TenantContext.getInstitutionId()));
    }
}
