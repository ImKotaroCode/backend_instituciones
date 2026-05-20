package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.*;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.PaymentsService;
import backend_instituciones.backend_instituciones.service.VoucherSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentsController {

    private final PaymentsService paymentsService;
    private final VoucherSubmissionService voucherSubmissionService;

    // ── A. Enrollment types ───────────────────────────────────────────────────

    @GetMapping("/enrollment-types")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<?> listEnrollmentTypes() {
        return ResponseEntity.ok(paymentsService.listEnrollmentTypes(TenantContext.getInstitutionId()));
    }

    @PostMapping("/enrollment-types")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> createEnrollmentType(@RequestBody EnrollmentTypeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentsService.createEnrollmentType(TenantContext.getInstitutionId(), req));
    }

    @PutMapping("/enrollment-types/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> updateEnrollmentType(@PathVariable Long id,
                                                   @RequestBody EnrollmentTypeRequest req) {
        return ResponseEntity.ok(
                paymentsService.updateEnrollmentType(id, TenantContext.getInstitutionId(), req));
    }

    // ── B. Monthly scales ─────────────────────────────────────────────────────

    @GetMapping("/monthly-scales")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PADRE','ADMINISTRACION')")
    public ResponseEntity<?> listMonthlyScales() {
        return ResponseEntity.ok(paymentsService.listMonthlyScales(TenantContext.getInstitutionId()));
    }

    @PostMapping("/monthly-scales")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> createMonthlyScale(@RequestBody MonthlyScaleRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentsService.createMonthlyScale(TenantContext.getInstitutionId(), req));
    }

    @PutMapping("/monthly-scales/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> updateMonthlyScale(@PathVariable Long id,
                                                 @RequestBody MonthlyScaleRequest req) {
        return ResponseEntity.ok(
                paymentsService.updateMonthlyScale(id, TenantContext.getInstitutionId(), req));
    }

    // ── C. Search student by DNI ──────────────────────────────────────────────

    @GetMapping("/students/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<?> searchStudent(@RequestParam String dni) {
        return ResponseEntity.ok(
                paymentsService.searchStudentByDni(TenantContext.getInstitutionId(), dni));
    }

    // ── D. Student payment profiles ───────────────────────────────────────────

    @GetMapping("/student-profiles")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PADRE','ADMINISTRACION')")
    public ResponseEntity<?> listProfiles(
            @RequestParam(required = false) Long academicYearId) {
        return ResponseEntity.ok(
                paymentsService.listProfiles(TenantContext.getInstitutionId(), academicYearId));
    }

    @PostMapping("/student-profiles")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> createOrUpdateProfile(@RequestBody StudentPaymentProfileRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentsService.createOrUpdateProfile(TenantContext.getInstitutionId(), req));
    }

    @PutMapping("/student-profiles/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> updateProfile(@PathVariable Long id,
                                            @RequestBody StudentPaymentProfileRequest req) {
        return ResponseEntity.ok(
                paymentsService.updateProfile(id, TenantContext.getInstitutionId(), req));
    }

    // ── E. Generate charges ───────────────────────────────────────────────────

    @PostMapping("/student-profiles/{profileId}/generate-charges")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> generateCharges(@PathVariable Long profileId) {
        return ResponseEntity.ok(
                paymentsService.generateCharges(profileId, TenantContext.getInstitutionId()));
    }

    // ── F/G. Charges ──────────────────────────────────────────────────────────

    @GetMapping("/charges")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PADRE','ADMINISTRACION')")
    public ResponseEntity<?> listCharges(
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) Long studentId) {
        return ResponseEntity.ok(
                paymentsService.listCharges(TenantContext.getInstitutionId(), academicYearId, studentId));
    }

    @PatchMapping("/charges/{chargeId}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> markPaid(@PathVariable Long chargeId,
                                       @RequestBody PayChargeRequest req) {
        return ResponseEntity.ok(
                paymentsService.markPaid(chargeId, TenantContext.getInstitutionId(), req));
    }

    // ── I. Voucher submissions (admin) ────────────────────────────────────────

    /**
     * GET /api/v1/payments/voucher-submissions/pending?academicYearId=1
     * Cards agrupadas por alumno con vouchers SUBMITTED. Alimenta "Validar Pagos".
     */
    @GetMapping("/voucher-submissions/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<?> listPendingVouchers(
            @RequestParam(required = false) Long academicYearId) {
        return ResponseEntity.ok(
                voucherSubmissionService.listPending(TenantContext.getInstitutionId(), academicYearId));
    }

    /**
     * GET /api/v1/payments/voucher-submissions?studentId=10&academicYearId=1&chargeId=91&status=SUBMITTED
     * Lista detallada de vouchers con filtros opcionales. Alimenta "Ver Detalles".
     */
    @GetMapping("/voucher-submissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PADRE','ADMINISTRACION')")
    public ResponseEntity<?> listVouchers(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) Long chargeId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(
                voucherSubmissionService.listByFilters(
                        TenantContext.getInstitutionId(), studentId, academicYearId, chargeId, status));
    }

    /**
     * PATCH /api/v1/payments/voucher-submissions/{voucherId}/review
     * Aprobar o rechazar un voucher.
     */
    @PatchMapping("/voucher-submissions/{voucherId}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> reviewVoucher(
            @PathVariable Long voucherId,
            @RequestBody VoucherReviewRequest req) {
        return ResponseEntity.ok(
                voucherSubmissionService.reviewVoucher(
                        voucherId, TenantContext.getInstitutionId(), req));
    }

    // ── H. Overview ───────────────────────────────────────────────────────────

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<?> getOverview(
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) Long levelId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Long sectionId) {
        return ResponseEntity.ok(
                paymentsService.getOverview(TenantContext.getInstitutionId(), academicYearId,
                        levelId, gradeId, sectionId));
    }
}
