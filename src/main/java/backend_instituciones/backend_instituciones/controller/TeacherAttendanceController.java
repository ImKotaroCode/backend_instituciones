package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.AutoCheckInRequest;
import backend_instituciones.backend_instituciones.dto.request.TeacherAttendanceSessionRequest;
import backend_instituciones.backend_instituciones.dto.response.TeacherAttendanceSessionResponse;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.TeacherAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/teacher-attendance")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ADMINISTRACION')")
public class TeacherAttendanceController {

    private final TeacherAttendanceService teacherAttendanceService;

    // ── GET /sessions ──────────────────────────────────────────────────────────

    @GetMapping("/sessions")
    public ResponseEntity<?> getSessions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long levelId,
            @RequestParam(required = false) Long gradeId) {
        return ResponseEntity.ok(teacherAttendanceService.getSessions(
                TenantContext.getInstitutionId(), date, dateFrom, dateTo,
                teacherId, courseId, sectionId, levelId, gradeId));
    }

    // ── GET /session (legacy single lookup) ───────────────────────────────────

    @GetMapping("/session")
    public ResponseEntity<?> getSession(
            @RequestParam Long teacherId,
            @RequestParam Long courseId,
            @RequestParam Long sectionId,
            @RequestParam String date) {
        Optional<TeacherAttendanceSessionResponse> result = teacherAttendanceService.getSession(
                TenantContext.getInstitutionId(), teacherId, courseId, sectionId, LocalDate.parse(date));
        return result.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(null));
    }

    // ── POST /session ──────────────────────────────────────────────────────────

    @PostMapping("/session")
    public ResponseEntity<?> createSession(@Valid @RequestBody TeacherAttendanceSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teacherAttendanceService.createSession(TenantContext.getInstitutionId(), request));
    }

    // ── PUT /session/{sessionId} ───────────────────────────────────────────────

    @PutMapping("/session/{sessionId}")
    public ResponseEntity<?> updateSession(@PathVariable Long sessionId,
                                           @Valid @RequestBody TeacherAttendanceSessionRequest request) {
        return ResponseEntity.ok(
                teacherAttendanceService.updateSession(TenantContext.getInstitutionId(), sessionId, request));
    }

    // ── POST /auto-check-in ────────────────────────────────────────────────────

    @PostMapping("/auto-check-in")
    public ResponseEntity<?> autoCheckIn(@Valid @RequestBody AutoCheckInRequest request) {
        return ResponseEntity.ok(
                teacherAttendanceService.autoCheckIn(
                        TenantContext.getInstitutionId(),
                        request.getTeacherId(),
                        request.getTriggeredAt()));
    }

    // ── GET /my-summary ────────────────────────────────────────────────────────

    @GetMapping("/my-summary")
    public ResponseEntity<?> getMySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        Long teacherId = TenantContext.getUserId();
        Long institutionId = TenantContext.getInstitutionId();
        if (dateFrom == null) dateFrom = LocalDate.now().withDayOfMonth(1);
        if (dateTo == null) dateTo = LocalDate.now();
        return ResponseEntity.ok(teacherAttendanceService.getMySummary(institutionId, teacherId, dateFrom, dateTo));
    }

    // ── GET /my-calendar ───────────────────────────────────────────────────────

    @GetMapping("/my-calendar")
    public ResponseEntity<?> getMyCalendar(
            @RequestParam(required = false) String month) {
        Long teacherId = TenantContext.getUserId();
        Long institutionId = TenantContext.getInstitutionId();
        if (month == null) {
            java.time.YearMonth ym = java.time.YearMonth.now();
            month = ym.toString();
        }
        return ResponseEntity.ok(teacherAttendanceService.getMyCalendar(institutionId, teacherId, month));
    }
}
