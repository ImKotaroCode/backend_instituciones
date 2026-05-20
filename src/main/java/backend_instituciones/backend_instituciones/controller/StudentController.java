package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.CourseAssessmentService;
import backend_instituciones.backend_instituciones.service.CourseAttendanceOverviewService;
import backend_instituciones.backend_instituciones.service.CourseTaskService;
import backend_instituciones.backend_instituciones.service.DashboardService;
import backend_instituciones.backend_instituciones.service.StudentGradesOverviewService;
import backend_instituciones.backend_instituciones.service.UserRelationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final UserRelationsService userRelationsService;
    private final CourseTaskService courseTaskService;
    private final CourseAssessmentService courseAssessmentService;
    private final DashboardService dashboardService;
    private final CourseAttendanceOverviewService courseAttendanceOverviewService;
    private final StudentGradesOverviewService studentGradesOverviewService;

    @GetMapping("/{studentId}/courses")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'DOCENTE', 'ESTUDIANTE', 'PADRE','ADMINISTRACION')")
    public ResponseEntity<?> getCourses(@PathVariable Long studentId) {
        return ResponseEntity.ok(userRelationsService.getStudentCourses(studentId, TenantContext.getInstitutionId()));
    }

    @GetMapping("/{studentId}/courses/{courseId}/tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'DOCENTE', 'ESTUDIANTE', 'PADRE','ADMINISTRACION')")
    public ResponseEntity<?> getStudentTasks(@PathVariable Long studentId,
                                              @PathVariable Long courseId) {
        return ResponseEntity.ok(courseTaskService.getStudentTasks(
                TenantContext.getInstitutionId(), studentId, courseId));
    }

    @GetMapping("/{studentId}/courses/{courseId}/assessments")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'DOCENTE', 'ESTUDIANTE', 'PADRE','ADMINISTRACION')")
    public ResponseEntity<?> getStudentAssessments(@PathVariable Long studentId,
                                                    @PathVariable Long courseId) {
        return ResponseEntity.ok(courseAssessmentService.getStudentAssessments(
                TenantContext.getInstitutionId(), studentId, courseId));
    }

    /**
     * GET /api/v1/students/{studentId}/courses/{courseId}/tasks/overview
     * ?periodId=2&status=ACTIVAS|PASADAS|TODAS
     */
    @GetMapping("/{studentId}/courses/{courseId}/tasks/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'DOCENTE', 'ESTUDIANTE', 'PADRE','ADMINISTRACION')")
    public ResponseEntity<?> getCourseTaskOverview(
            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false, defaultValue = "TODAS") String status) {
        return ResponseEntity.ok(courseTaskService.getCourseTaskOverview(
                TenantContext.getInstitutionId(), studentId, courseId, periodId, status));
    }

    /**
     * GET /api/v1/students/{studentId}/courses/{courseId}/attendance/overview
     * ?periodId=2&sectionId=3&dateFrom=2026-05-11&dateTo=2026-05-17
     */
    @GetMapping("/{studentId}/courses/{courseId}/attendance/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'DOCENTE', 'ESTUDIANTE', 'PADRE','ADMINISTRACION')")
    public ResponseEntity<?> getAttendanceOverview(
            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(courseAttendanceOverviewService.getStudentOverview(
                TenantContext.getInstitutionId(), studentId, courseId, periodId, sectionId, dateFrom, dateTo));
    }

    /**
     * GET /api/v1/students/{studentId}/grades/overview
     * ?periodId=2&academicYearId=1
     */
    @GetMapping("/{studentId}/grades/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'DOCENTE', 'ESTUDIANTE', 'PADRE','ADMINISTRACION')")
    public ResponseEntity<?> getGradesOverview(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) Long academicYearId) {
        return ResponseEntity.ok(studentGradesOverviewService.getOverview(
                TenantContext.getInstitutionId(), studentId, periodId, academicYearId));
    }

    /** GET /api/v1/students/{studentId}/tasks/upcoming-pending?activeOnly=true&limit=10 */
    @GetMapping("/{studentId}/tasks/upcoming-pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'DOCENTE', 'ESTUDIANTE', 'PADRE','ADMINISTRACION')")
    public ResponseEntity<?> getUpcomingPending(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(dashboardService.getUpcomingPending(
                TenantContext.getInstitutionId(), studentId, activeOnly, limit));
    }
}
