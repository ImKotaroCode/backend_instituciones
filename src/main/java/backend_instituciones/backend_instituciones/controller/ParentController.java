package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.ParentPortalService;
import backend_instituciones.backend_instituciones.service.PaymentsService;
import backend_instituciones.backend_instituciones.service.StudentGradesOverviewService;
import backend_instituciones.backend_instituciones.service.VoucherSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parent portal — mobile-first, scoped to parent's own children.
 * <p>
 * Security rules enforced in {@link ParentPortalService}:
 * <ul>
 *   <li>parentId must match the authenticated user (JWT userId)</li>
 *   <li>studentId must be linked to parentId via ParentStudentLink</li>
 *   <li>courseId must belong to the student's section</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/parents")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PADRE', 'ADMIN', 'DIRECTOR','ADMINISTRACION')")
public class ParentController {

    private final ParentPortalService parentPortalService;
    private final PaymentsService paymentsService;
    private final VoucherSubmissionService voucherSubmissionService;
    private final StudentGradesOverviewService studentGradesOverviewService;

    /** GET /api/v1/parents/{parentId}/children */
    @GetMapping("/{parentId}/children")
    public ResponseEntity<?> getChildren(@PathVariable Long parentId) {
        return ResponseEntity.ok(
                parentPortalService.getChildren(parentId, TenantContext.getInstitutionId()));
    }

    /** GET /api/v1/parents/{parentId}/children/{studentId}/courses */
    @GetMapping("/{parentId}/children/{studentId}/courses")
    public ResponseEntity<?> getChildCourses(
            @PathVariable Long parentId,
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(
                parentPortalService.getChildCourses(
                        parentId, studentId, TenantContext.getInstitutionId(), includeInactive));
    }

    /**
     * GET /api/v1/parents/{parentId}/children/{studentId}/courses/{courseId}/tasks/overview
     * ?periodId=2&status=ACTIVAS|PASADAS|TODAS
     */
    @GetMapping("/{parentId}/children/{studentId}/courses/{courseId}/tasks/overview")
    public ResponseEntity<?> getChildTasksOverview(
            @PathVariable Long parentId,
            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false, defaultValue = "TODAS") String status) {
        return ResponseEntity.ok(
                parentPortalService.getChildTasksOverview(
                        parentId, studentId, courseId,
                        TenantContext.getInstitutionId(), periodId, status));
    }

    /**
     * GET /api/v1/parents/{parentId}/children/{studentId}/courses/{courseId}/attendance/overview
     * ?sectionId=3&periodId=2&dateFrom=2026-05-11&dateTo=2026-05-17
     */
    @GetMapping("/{parentId}/children/{studentId}/courses/{courseId}/attendance/overview")
    public ResponseEntity<?> getChildAttendanceOverview(
            @PathVariable Long parentId,
            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(
                parentPortalService.getChildAttendanceOverview(
                        parentId, studentId, courseId,
                        TenantContext.getInstitutionId(),
                        periodId, sectionId, dateFrom, dateTo));
    }

    /**
     * GET /api/v1/parents/{parentId}/children/{studentId}/grades/overview
     * ?periodId=2&academicYearId=1
     */
    @GetMapping("/{parentId}/children/{studentId}/grades/overview")
    public ResponseEntity<?> getChildGradesOverviewAll(
            @PathVariable Long parentId,
            @PathVariable Long studentId,
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) Long academicYearId) {
        parentPortalService.assertParentChildPublic(parentId, studentId, TenantContext.getInstitutionId());
        return ResponseEntity.ok(studentGradesOverviewService.getOverview(
                TenantContext.getInstitutionId(), studentId, periodId, academicYearId));
    }

    /**
     * GET /api/v1/parents/{parentId}/children/{studentId}/courses/{courseId}/grades/overview
     * ?periodId=2
     */
    @GetMapping("/{parentId}/children/{studentId}/courses/{courseId}/grades/overview")
    public ResponseEntity<?> getChildGradesOverview(
            @PathVariable Long parentId,
            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @RequestParam(required = false) Long periodId) {
        return ResponseEntity.ok(
                parentPortalService.getChildGradesOverview(
                        parentId, studentId, courseId,
                        TenantContext.getInstitutionId(), periodId));
    }

    /**
     * GET /api/v1/parents/{parentId}/children/{studentId}/account-status
     * Estado de cuenta del hijo — pagos, mora, etc.
     */
    @GetMapping("/{parentId}/children/{studentId}/account-status")
    public ResponseEntity<?> getAccountStatus(
            @PathVariable Long parentId,
            @PathVariable Long studentId) {
        parentPortalService.assertParentChildPublic(parentId, studentId, TenantContext.getInstitutionId());
        return ResponseEntity.ok(
                paymentsService.getAccountStatus(studentId, TenantContext.getInstitutionId()));
    }

    // ── Vouchers ──────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/parents/{parentId}/children/{studentId}/vouchers?academicYearId=1
     * Lista vouchers del hijo.
     */
    @GetMapping("/{parentId}/children/{studentId}/vouchers")
    public ResponseEntity<?> listVouchers(
            @PathVariable Long parentId,
            @PathVariable Long studentId,
            @RequestParam(required = false) Long academicYearId) {
        parentPortalService.assertParentChildPublic(parentId, studentId, TenantContext.getInstitutionId());
        return ResponseEntity.ok(
                voucherSubmissionService.listVouchersForStudent(
                        studentId, TenantContext.getInstitutionId(), academicYearId));
    }

    /**
     * POST /api/v1/parents/{parentId}/children/{studentId}/charges/{chargeId}/voucher
     * Sube (o reemplaza) voucher de pago para un cargo.
     */
    @PostMapping(value = "/{parentId}/children/{studentId}/charges/{chargeId}/voucher",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitVoucher(
            @PathVariable Long parentId,
            @PathVariable Long studentId,
            @PathVariable Long chargeId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam("file") MultipartFile file) {
        parentPortalService.assertParentChildPublic(parentId, studentId, TenantContext.getInstitutionId());
        return ResponseEntity.ok(
                voucherSubmissionService.submitVoucher(
                        parentId, studentId, chargeId,
                        TenantContext.getInstitutionId(), academicYearId, file));
    }

    /**
     * GET /api/v1/parents/{parentId}/children/payment-status?studentIds=10,11,12
     * Resumen de estado de pagos por hijo — alimenta dashboard "Selecciona a tu hijo".
     */
    @GetMapping("/{parentId}/children/payment-status")
    public ResponseEntity<?> getChildrenPaymentStatus(
            @PathVariable Long parentId,
            @RequestParam(required = false) String studentIds) {
        List<Long> ids = studentIds != null && !studentIds.isBlank()
                ? Arrays.stream(studentIds.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toList())
                : List.of();
        return ResponseEntity.ok(
                voucherSubmissionService.getChildrenPaymentStatus(
                        TenantContext.getInstitutionId(), ids));
    }
}
