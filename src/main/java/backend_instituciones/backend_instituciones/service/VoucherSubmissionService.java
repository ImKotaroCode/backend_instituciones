package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.*;
import backend_instituciones.backend_instituciones.domain.enums.Role;
import backend_instituciones.backend_instituciones.dto.request.VoucherReviewRequest;
import backend_instituciones.backend_instituciones.dto.response.*;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VoucherSubmissionService {

    private final PaymentVoucherSubmissionRepository voucherRepo;
    private final PaymentChargeRepository chargeRepo;
    private final UserRepository userRepo;
    private final SupabaseStorageService storageService;
    private final SseService sseService;

    // ── 1. Parent: submit / replace voucher ──────────────────────────────────

    @Transactional
    public VoucherSubmissionResponse submitVoucher(Long parentId,
                                                    Long studentId,
                                                    Long chargeId,
                                                    Long institutionId,
                                                    Long academicYearId,
                                                    MultipartFile file) {
        // Validate charge belongs to student + institution
        PaymentCharge charge = chargeRepo.findByIdAndInstitutionId(chargeId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentCharge", chargeId));

        if (!charge.getStudentId().equals(studentId)) {
            throw new BusinessException("Charge does not belong to student",
                    HttpStatus.FORBIDDEN, "FORBIDDEN");
        }

        // Upload to Supabase (auto-converted to WEBP by SupabaseStorageService)
        String imageUrl = storageService.upload(file, "vouchers");
        String imageName = deriveImageName(file.getOriginalFilename(), chargeId);

        // Load student info for denormalization
        User student = userRepo.findById(studentId).orElse(null);
        String studentName   = student != null ? student.getName()       : null;
        String studentPhoto  = student != null ? student.getPhotoUrl()   : null;
        String studentDni    = student != null ? student.getDocumentNumber() : null;

        // Upsert — 1 voucher per charge
        PaymentVoucherSubmission voucher = voucherRepo.findByChargeId(chargeId)
                .orElseGet(() -> PaymentVoucherSubmission.builder()
                        .institutionId(institutionId)
                        .chargeId(chargeId)
                        .studentId(studentId)
                        .parentId(parentId)
                        .build());

        voucher.setAcademicYearId(academicYearId != null ? academicYearId : charge.getAcademicYearId());
        voucher.setStudentName(studentName);
        voucher.setStudentPhotoUrl(studentPhoto);
        voucher.setStudentDni(studentDni);
        voucher.setImageUrl(imageUrl);
        voucher.setImageName(imageName);
        voucher.setStatus("SUBMITTED");
        voucher.setSubmittedAt(LocalDateTime.now());
        voucher.setReviewedAt(null);
        voucher.setReviewedBy(null);
        voucher.setReviewerName(null);
        voucher.setReviewNote(null);

        PaymentVoucherSubmission saved = voucherRepo.save(voucher);

        // SSE → notify all ADMIN + DIRECTOR of institution
        notifyAdmins(institutionId, "payment_submission_created", Map.of(
                "type", "PAGO_ENVIADO",
                "id", saved.getId(),
                "chargeId", chargeId,
                "studentId", studentId,
                "studentName", studentName != null ? studentName : "",
                "studentPhotoUrl", studentPhoto != null ? studentPhoto : "",
                "studentDni", studentDni != null ? studentDni : "",
                "academicYearId", saved.getAcademicYearId() != null ? saved.getAcademicYearId() : 0,
                "chargeLabel", charge.getLabel(),
                "submittedAt", saved.getSubmittedAt().toString()
        ));

        return toResponse(saved);
    }

    // ── 2. Parent: list vouchers for a child ─────────────────────────────────

    public List<VoucherSubmissionResponse> listVouchersForStudent(Long studentId,
                                                                   Long institutionId,
                                                                   Long academicYearId) {
        List<PaymentVoucherSubmission> vouchers = academicYearId != null
                ? voucherRepo.findByInstitutionIdAndStudentIdAndAcademicYearIdOrderBySubmittedAtDesc(
                        institutionId, studentId, academicYearId)
                : voucherRepo.findByInstitutionIdAndStudentIdOrderBySubmittedAtDesc(institutionId, studentId);

        return vouchers.stream().map(this::toResponse).toList();
    }

    // ── 3. Parent dashboard: payment status summary per child ─────────────────

    public List<ChildPaymentStatusResponse> getChildrenPaymentStatus(Long institutionId,
                                                                      List<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) return List.of();

        List<PaymentVoucherSubmission> all = voucherRepo
                .findByInstitutionIdAndStudentIdIn(institutionId, studentIds);

        Map<Long, List<PaymentVoucherSubmission>> byStudent = all.stream()
                .collect(Collectors.groupingBy(PaymentVoucherSubmission::getStudentId));

        return studentIds.stream().map(sid -> {
            List<PaymentVoucherSubmission> sv = byStudent.getOrDefault(sid, List.of());

            long pending  = sv.stream().filter(v -> "SUBMITTED".equals(v.getStatus())).count();
            long approved = sv.stream().filter(v -> "APPROVED".equals(v.getStatus())).count();
            long rejected = sv.stream().filter(v -> "REJECTED".equals(v.getStatus())).count();

            String latestStatus = sv.stream()
                    .filter(v -> v.getSubmittedAt() != null)
                    .max(Comparator.comparing(PaymentVoucherSubmission::getSubmittedAt))
                    .map(PaymentVoucherSubmission::getStatus)
                    .orElse(null);

            return ChildPaymentStatusResponse.builder()
                    .studentId(sid)
                    .pendingSubmissionCount((int) pending)
                    .approvedCount((int) approved)
                    .rejectedCount((int) rejected)
                    .latestStatus(latestStatus)
                    .build();
        }).toList();
    }

    // ── 4. Admin: pending voucher cards (grouped by student) ─────────────────

    public List<PendingVoucherCardResponse> listPending(Long institutionId, Long academicYearId) {
        List<PaymentVoucherSubmission> submitted = academicYearId != null
                ? voucherRepo.findByInstitutionIdAndAcademicYearIdAndStatusOrderBySubmittedAtDesc(
                        institutionId, academicYearId, "SUBMITTED")
                : List.of();

        if (submitted.isEmpty()) return List.of();

        // Load charge labels in batch
        Set<Long> chargeIds = submitted.stream().map(PaymentVoucherSubmission::getChargeId)
                .collect(Collectors.toSet());
        Map<Long, String> chargeLabels = chargeRepo.findAllById(chargeIds).stream()
                .collect(Collectors.toMap(PaymentCharge::getId, PaymentCharge::getLabel));

        // Group by student
        Map<Long, List<PaymentVoucherSubmission>> byStudent = submitted.stream()
                .collect(Collectors.groupingBy(PaymentVoucherSubmission::getStudentId));

        return byStudent.entrySet().stream().map(e -> {
            Long sid = e.getKey();
            List<PaymentVoucherSubmission> sv = e.getValue();
            PaymentVoucherSubmission latest = sv.stream()
                    .max(Comparator.comparing(v -> v.getSubmittedAt() != null
                            ? v.getSubmittedAt() : LocalDateTime.MIN))
                    .orElse(sv.get(0));

            List<PendingVoucherCardResponse.VoucherItem> items = sv.stream()
                    .map(v -> PendingVoucherCardResponse.VoucherItem.builder()
                            .id(v.getId())
                            .chargeId(v.getChargeId())
                            .chargeLabel(chargeLabels.get(v.getChargeId()))
                            .imageUrl(v.getImageUrl())
                            .imageName(v.getImageName())
                            .status(v.getStatus())
                            .submittedAt(v.getSubmittedAt())
                            .build())
                    .toList();

            return PendingVoucherCardResponse.builder()
                    .studentId(sid)
                    .studentName(latest.getStudentName())
                    .studentPhotoUrl(latest.getStudentPhotoUrl())
                    .studentDni(latest.getStudentDni())
                    .academicYearId(latest.getAcademicYearId())
                    .alertCount(sv.size())
                    .latestSubmittedAt(latest.getSubmittedAt())
                    .vouchers(items)
                    .build();
        }).sorted(Comparator.comparing(PendingVoucherCardResponse::getLatestSubmittedAt,
                Comparator.nullsLast(Comparator.reverseOrder())))
        .toList();
    }

    // ── 5. Admin: list vouchers with optional filters ─────────────────────────

    public List<VoucherSubmissionResponse> listByFilters(Long institutionId,
                                                          Long studentId,
                                                          Long academicYearId,
                                                          Long chargeId,
                                                          String status) {
        List<PaymentVoucherSubmission> vouchers;

        if (studentId != null && academicYearId != null && status != null) {
            vouchers = voucherRepo
                    .findByInstitutionIdAndStudentIdAndAcademicYearIdAndStatusOrderBySubmittedAtDesc(
                            institutionId, studentId, academicYearId, status);
        } else if (studentId != null && academicYearId != null) {
            vouchers = voucherRepo
                    .findByInstitutionIdAndStudentIdAndAcademicYearIdOrderBySubmittedAtDesc(
                            institutionId, studentId, academicYearId);
        } else if (studentId != null) {
            vouchers = voucherRepo
                    .findByInstitutionIdAndStudentIdOrderBySubmittedAtDesc(institutionId, studentId);
        } else if (academicYearId != null) {
            vouchers = voucherRepo
                    .findByInstitutionIdAndAcademicYearIdOrderBySubmittedAtDesc(institutionId, academicYearId);
        } else {
            vouchers = List.of();
        }

        // Optional in-memory filter by chargeId / status when not already applied
        if (chargeId != null) {
            final Long cid = chargeId;
            vouchers = vouchers.stream().filter(v -> cid.equals(v.getChargeId())).toList();
        }
        if (status != null && !(studentId != null && academicYearId != null)) {
            final String st = status;
            vouchers = vouchers.stream().filter(v -> st.equalsIgnoreCase(v.getStatus())).toList();
        }

        return vouchers.stream().map(this::toResponse).toList();
    }

    // ── 6. Admin: approve or reject voucher ──────────────────────────────────

    @Transactional
    public VoucherSubmissionResponse reviewVoucher(Long voucherId,
                                                    Long institutionId,
                                                    VoucherReviewRequest req) {
        PaymentVoucherSubmission voucher = voucherRepo.findByIdAndInstitutionId(voucherId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("VoucherSubmission", voucherId));

        if (!"SUBMITTED".equals(voucher.getStatus())) {
            throw new BusinessException(
                    "Voucher already reviewed (status=" + voucher.getStatus() + ")",
                    HttpStatus.CONFLICT, "ALREADY_REVIEWED");
        }

        String newStatus = req.getStatus();
        if (!"APPROVED".equals(newStatus) && !"REJECTED".equals(newStatus)) {
            throw new BusinessException("status must be APPROVED or REJECTED",
                    HttpStatus.BAD_REQUEST, "INVALID_STATUS");
        }

        voucher.setStatus(newStatus);
        voucher.setReviewedAt(LocalDateTime.now());
        voucher.setReviewedBy(req.getReviewedBy());
        voucher.setReviewerName(req.getReviewerName());
        voucher.setReviewNote(req.getNote());

        if ("APPROVED".equals(newStatus)) {
            // Mark associated charge as PAID
            chargeRepo.findById(voucher.getChargeId()).ifPresent(charge -> {
                charge.setStatus("PAID");
                charge.setPaidAt(LocalDate.now());
                charge.setPaidAmount(charge.getBaseAmount()); // base amount; mora not stored
                chargeRepo.save(charge);
            });

            // SSE → notify parent
            sseService.sendToUser(voucher.getParentId().toString(),
                    "payment_submission_approved",
                    Map.of(
                            "type", "PAGO_APROBADO",
                            "id", voucher.getId(),
                            "chargeId", voucher.getChargeId(),
                            "studentId", voucher.getStudentId(),
                            "reviewedAt", voucher.getReviewedAt().toString(),
                            "reviewerName", safe(req.getReviewerName()),
                            "reviewNote", safe(req.getNote())
                    ));
        } else {
            // SSE → notify parent
            sseService.sendToUser(voucher.getParentId().toString(),
                    "payment_submission_rejected",
                    Map.of(
                            "type", "PAGO_RECHAZADO",
                            "id", voucher.getId(),
                            "chargeId", voucher.getChargeId(),
                            "studentId", voucher.getStudentId(),
                            "reviewedAt", voucher.getReviewedAt().toString(),
                            "reviewerName", safe(req.getReviewerName()),
                            "reviewNote", safe(req.getNote())
                    ));
        }

        return toResponse(voucherRepo.save(voucher));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void notifyAdmins(Long institutionId, String eventName, Map<String, Object> data) {
        List<User> admins = userRepo.findByInstitutionIdAndRole(institutionId, Role.ADMIN);
        List<User> directors = userRepo.findByInstitutionIdAndRole(institutionId, Role.DIRECTOR);

        List<User> targets = new ArrayList<>(admins);
        targets.addAll(directors);

        targets.stream()
                .filter(User::isActive)
                .map(u -> u.getId().toString())
                .distinct()
                .forEach(uid -> sseService.sendToUser(uid, eventName, data));
    }

    private VoucherSubmissionResponse toResponse(PaymentVoucherSubmission v) {
        return VoucherSubmissionResponse.builder()
                .id(v.getId())
                .chargeId(v.getChargeId())
                .studentId(v.getStudentId())
                .studentName(v.getStudentName())
                .studentPhotoUrl(v.getStudentPhotoUrl())
                .studentDni(v.getStudentDni())
                .academicYearId(v.getAcademicYearId())
                .parentId(v.getParentId())
                .imageUrl(v.getImageUrl())
                .imageName(v.getImageName())
                .status(v.getStatus())
                .submittedAt(v.getSubmittedAt())
                .reviewedAt(v.getReviewedAt())
                .reviewedBy(v.getReviewedBy())
                .reviewerName(v.getReviewerName())
                .reviewNote(v.getReviewNote())
                .build();
    }

    private String deriveImageName(String originalFilename, Long chargeId) {
        return "voucher-cargo-" + chargeId + ".webp";
    }

    private String safe(String s) {
        return s != null ? s : "";
    }
}
