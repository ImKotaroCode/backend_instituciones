package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.domain.enums.AttendanceStatus;
import backend_instituciones.backend_instituciones.dto.request.AttendanceBulkRequest;
import backend_instituciones.backend_instituciones.dto.request.StudentAttendanceRecordRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.AttendanceCenterService;
import backend_instituciones.backend_instituciones.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceCenterService attendanceCenterService;

    // ── legacy endpoints ──────────────────────────────────────────────────────

    @GetMapping("/course/{courseId}/date/{date}")
    @PreAuthorize("hasAnyRole('DOCENTE','ADMIN','DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<?> getByCourseAndDate(@PathVariable Long courseId,
                                                @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getByCourseAndDate(courseId, date));
    }

    @PostMapping("/legacy-bulk")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<?> legacyBulk(@Valid @RequestBody AttendanceBulkRequest request) {
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
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ESTUDIANTE','PADRE','ADMINISTRACION')")
    public ResponseEntity<?> summary(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceService.getSummary(studentId, TenantContext.getInstitutionId()));
    }

    // ── attendance center endpoints ───────────────────────────────────────────

    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ESTUDIANTE','PADRE','ADMINISTRACION')")
    public ResponseEntity<?> getRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long teacherId) {
        return ResponseEntity.ok(attendanceCenterService.getRecords(
                TenantContext.getInstitutionId(), date, dateFrom, dateTo,
                studentId, courseId, sectionId, teacherId));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ADMINISTRACION')")
    public ResponseEntity<?> bulkCreate(@RequestBody List<StudentAttendanceRecordRequest> requests) {
        return ResponseEntity.ok(attendanceCenterService.bulkCreate(
                TenantContext.getInstitutionId(), requests));
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<?> getAlerts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long levelId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long teacherId) {
        return ResponseEntity.ok(attendanceCenterService.getAlerts(
                TenantContext.getInstitutionId(), date, levelId, gradeId, sectionId, teacherId));
    }

    @GetMapping("/person-summary")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ADMINISTRACION')")
    public ResponseEntity<?> getPersonSummary(
            @RequestParam String personType,
            @RequestParam Long personId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        if (dateFrom == null) dateFrom = LocalDate.now().withDayOfMonth(1);
        if (dateTo == null) dateTo = LocalDate.now();
        return ResponseEntity.ok(attendanceCenterService.getPersonSummary(
                TenantContext.getInstitutionId(), personType, personId, dateFrom, dateTo));
    }
}
