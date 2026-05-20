package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.*;
import backend_instituciones.backend_instituciones.dto.request.*;
import backend_instituciones.backend_instituciones.dto.response.*;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentsService {

    private final PaymentEnrollmentTypeRepository enrollmentTypeRepo;
    private final PaymentMonthlyScaleRepository monthlyScaleRepo;
    private final PaymentStudentProfileRepository profileRepo;
    private final PaymentChargeRepository chargeRepo;
    private final AcademicYearRepository academicYearRepo;
    private final AcademicLevelRepository levelRepo;
    private final AcademicGradeRepository gradeRepo;
    private final AcademicSectionRepository sectionRepo;
    private final UserRepository userRepo;
    private final StudentSectionAssignmentRepository sectionAssignmentRepo;

    // ── A. Enrollment types ───────────────────────────────────────────────────

    public List<EnrollmentTypeResponse> listEnrollmentTypes(Long institutionId) {
        return enrollmentTypeRepo.findByInstitutionIdOrderByNameAsc(institutionId)
                .stream().map(this::toEnrollmentTypeResponse).toList();
    }

    @Transactional
    public EnrollmentTypeResponse createEnrollmentType(Long institutionId, EnrollmentTypeRequest req) {
        PaymentEnrollmentType et = PaymentEnrollmentType.builder()
                .institutionId(institutionId)
                .name(req.getName())
                .description(req.getDescription())
                .amount(req.getAmount())
                .active(req.isActive())
                .build();
        return toEnrollmentTypeResponse(enrollmentTypeRepo.save(et));
    }

    @Transactional
    public EnrollmentTypeResponse updateEnrollmentType(Long id, Long institutionId,
                                                        EnrollmentTypeRequest req) {
        PaymentEnrollmentType et = enrollmentTypeRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("EnrollmentType", id));
        et.setName(req.getName());
        et.setDescription(req.getDescription());
        et.setAmount(req.getAmount());
        et.setActive(req.isActive());
        return toEnrollmentTypeResponse(enrollmentTypeRepo.save(et));
    }

    // ── B. Monthly scales ─────────────────────────────────────────────────────

    public List<MonthlyScaleResponse> listMonthlyScales(Long institutionId) {
        return monthlyScaleRepo.findByInstitutionIdOrderByNameAsc(institutionId)
                .stream().map(this::toMonthlyScaleResponse).toList();
    }

    @Transactional
    public MonthlyScaleResponse createMonthlyScale(Long institutionId, MonthlyScaleRequest req) {
        PaymentMonthlyScale scale = PaymentMonthlyScale.builder()
                .institutionId(institutionId)
                .name(req.getName())
                .monthlyAmount(req.getMonthlyAmount())
                .lateMode(req.getLateMode() != null ? req.getLateMode() : "NONE")
                .lateAmount(req.getLateAmount() != null ? req.getLateAmount() : BigDecimal.ZERO)
                .active(req.isActive())
                .build();
        return toMonthlyScaleResponse(monthlyScaleRepo.save(scale));
    }

    @Transactional
    public MonthlyScaleResponse updateMonthlyScale(Long id, Long institutionId,
                                                    MonthlyScaleRequest req) {
        PaymentMonthlyScale scale = monthlyScaleRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("MonthlyScale", id));
        scale.setName(req.getName());
        scale.setMonthlyAmount(req.getMonthlyAmount());
        scale.setLateMode(req.getLateMode() != null ? req.getLateMode() : "NONE");
        scale.setLateAmount(req.getLateAmount() != null ? req.getLateAmount() : BigDecimal.ZERO);
        scale.setActive(req.isActive());
        return toMonthlyScaleResponse(monthlyScaleRepo.save(scale));
    }

    // ── C. Search student by DNI ──────────────────────────────────────────────

    public Map<String, Object> searchStudentByDni(Long institutionId, String dni) {
        Optional<User> userOpt = userRepo.findByDocumentNumberAndInstitutionId(dni, institutionId);
        if (userOpt.isEmpty()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("student", null);
            return empty;
        }
        User u = userOpt.get();
        Optional<StudentSectionAssignment> asgOpt =
                sectionAssignmentRepo.findByInstitutionIdAndStudentId(institutionId, u.getId());

        Map<String, Object> student = new LinkedHashMap<>();
        student.put("id", u.getId());
        student.put("name", u.getName());
        student.put("firstName", u.getFirstName());
        student.put("lastName", u.getLastName());
        student.put("documentNumber", u.getDocumentNumber());
        student.put("status", u.isActive() ? "ACTIVE" : "INACTIVE");

        if (asgOpt.isPresent()) {
            StudentSectionAssignment asg = asgOpt.get();
            student.put("levelId", asg.getLevelId());
            student.put("gradeId", asg.getGradeId());
            student.put("sectionId", asg.getSectionId());
            student.put("levelName", levelRepo.findById(asg.getLevelId()).map(AcademicLevel::getName).orElse(null));
            student.put("gradeName", gradeRepo.findById(asg.getGradeId()).map(AcademicGrade::getName).orElse(null));
            student.put("sectionName", sectionRepo.findById(asg.getSectionId()).map(AcademicSection::getName).orElse(null));
        } else {
            student.put("levelId", null);
            student.put("gradeId", null);
            student.put("sectionId", null);
            student.put("levelName", null);
            student.put("gradeName", null);
            student.put("sectionName", null);
        }

        return Map.of("student", student);
    }

    // ── D. Student payment profiles ───────────────────────────────────────────

    public List<StudentPaymentProfileResponse> listProfiles(Long institutionId, Long academicYearId) {
        List<PaymentStudentProfile> profiles = academicYearId != null
                ? profileRepo.findByInstitutionIdAndAcademicYearIdOrderByStudentNameAsc(institutionId, academicYearId)
                : profileRepo.findByInstitutionIdOrderByStudentNameAsc(institutionId);
        return profiles.stream().map(this::toProfileResponse).toList();
    }

    @Transactional
    public StudentPaymentProfileResponse createOrUpdateProfile(Long institutionId,
                                                                StudentPaymentProfileRequest req) {
        // Validate referenced entities exist in this institution
        enrollmentTypeRepo.findByIdAndInstitutionId(req.getEnrollmentTypeId(), institutionId)
                .orElseThrow(() -> new BusinessException("EnrollmentType not found",
                        HttpStatus.NOT_FOUND, "NOT_FOUND"));
        monthlyScaleRepo.findByIdAndInstitutionId(req.getMonthlyScaleId(), institutionId)
                .orElseThrow(() -> new BusinessException("MonthlyScale not found",
                        HttpStatus.NOT_FOUND, "NOT_FOUND"));

        // Upsert: one profile per student+academicYear
        PaymentStudentProfile profile = profileRepo
                .findByInstitutionIdAndStudentIdAndAcademicYearId(
                        institutionId, req.getStudentId(), req.getAcademicYearId())
                .orElseGet(() -> PaymentStudentProfile.builder()
                        .institutionId(institutionId)
                        .studentId(req.getStudentId())
                        .academicYearId(req.getAcademicYearId())
                        .build());

        profile.setStudentName(req.getStudentName());
        profile.setStudentDni(req.getStudentDni());
        profile.setAcademicYearName(req.getAcademicYearName());
        profile.setEnrollmentTypeId(req.getEnrollmentTypeId());
        profile.setMonthlyScaleId(req.getMonthlyScaleId());
        profile.setLevelId(req.getLevelId());
        profile.setGradeId(req.getGradeId());
        profile.setSectionId(req.getSectionId());
        profile.setLevelName(req.getLevelName());
        profile.setGradeName(req.getGradeName());
        profile.setSectionName(req.getSectionName());

        PaymentStudentProfile saved = profileRepo.save(profile);
        generateCharges(saved.getId(), institutionId);
        return toProfileResponse(saved);
    }

    @Transactional
    public StudentPaymentProfileResponse updateProfile(Long id, Long institutionId,
                                                        StudentPaymentProfileRequest req) {
        PaymentStudentProfile profile = profileRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentPaymentProfile", id));

        enrollmentTypeRepo.findByIdAndInstitutionId(req.getEnrollmentTypeId(), institutionId)
                .orElseThrow(() -> new BusinessException("EnrollmentType not found",
                        HttpStatus.NOT_FOUND, "NOT_FOUND"));
        monthlyScaleRepo.findByIdAndInstitutionId(req.getMonthlyScaleId(), institutionId)
                .orElseThrow(() -> new BusinessException("MonthlyScale not found",
                        HttpStatus.NOT_FOUND, "NOT_FOUND"));

        profile.setStudentName(req.getStudentName());
        profile.setStudentDni(req.getStudentDni());
        profile.setAcademicYearName(req.getAcademicYearName());
        profile.setEnrollmentTypeId(req.getEnrollmentTypeId());
        profile.setMonthlyScaleId(req.getMonthlyScaleId());
        profile.setLevelId(req.getLevelId());
        profile.setGradeId(req.getGradeId());
        profile.setSectionId(req.getSectionId());
        profile.setLevelName(req.getLevelName());
        profile.setGradeName(req.getGradeName());
        profile.setSectionName(req.getSectionName());

        PaymentStudentProfile saved = profileRepo.save(profile);
        generateCharges(saved.getId(), institutionId);
        return toProfileResponse(saved);
    }

    // ── E. Generate charges ───────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> generateCharges(Long profileId, Long institutionId) {
        PaymentStudentProfile profile = profileRepo.findByIdAndInstitutionId(profileId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentPaymentProfile", profileId));

        AcademicYear year = academicYearRepo.findByIdAndInstitutionId(
                        profile.getAcademicYearId(), institutionId)
                .orElseThrow(() -> new BusinessException("Academic year not found",
                        HttpStatus.NOT_FOUND, "NOT_FOUND"));

        PaymentEnrollmentType et = enrollmentTypeRepo
                .findByIdAndInstitutionId(profile.getEnrollmentTypeId(), institutionId)
                .orElseThrow(() -> new BusinessException("EnrollmentType not found",
                        HttpStatus.NOT_FOUND, "NOT_FOUND"));

        PaymentMonthlyScale scale = monthlyScaleRepo
                .findByIdAndInstitutionId(profile.getMonthlyScaleId(), institutionId)
                .orElseThrow(() -> new BusinessException("MonthlyScale not found",
                        HttpStatus.NOT_FOUND, "NOT_FOUND"));

        int generated = 0;

        // 1. Enrollment charge — one-time
        if (!chargeRepo.existsByProfileIdAndKind(profileId, "ENROLLMENT")) {
            LocalDate enrollDue = year.getStartDate() != null ? year.getStartDate() : LocalDate.now();
            chargeRepo.save(PaymentCharge.builder()
                    .institutionId(institutionId)
                    .studentId(profile.getStudentId())
                    .academicYearId(profile.getAcademicYearId())
                    .profileId(profileId)
                    .kind("ENROLLMENT")
                    .label(et.getName())
                    .dueDate(enrollDue)
                    .baseAmount(et.getAmount())
                    .enrollmentTypeId(et.getId())
                    .levelId(profile.getLevelId())
                    .gradeId(profile.getGradeId())
                    .sectionId(profile.getSectionId())
                    .build());
            generated++;
        }

        // 2. Monthly charges — from first day of month after startDate, through endDate month
        if (year.getStartDate() != null && year.getEndDate() != null) {
            LocalDate cursor = year.getStartDate().withDayOfMonth(1).plusMonths(1);
            LocalDate endMonth = year.getEndDate().withDayOfMonth(1);

            while (!cursor.isAfter(endMonth)) {
                String monthKey = cursor.toString().substring(0, 7); // "2026-05"
                if (!chargeRepo.existsByProfileIdAndKindAndMonthKey(profileId, "MONTHLY", monthKey)) {
                    String label = buildMonthlyLabel(cursor);
                    chargeRepo.save(PaymentCharge.builder()
                            .institutionId(institutionId)
                            .studentId(profile.getStudentId())
                            .academicYearId(profile.getAcademicYearId())
                            .profileId(profileId)
                            .kind("MONTHLY")
                            .label(label)
                            .monthKey(monthKey)
                            .dueDate(cursor)
                            .baseAmount(scale.getMonthlyAmount())
                            .monthlyScaleId(scale.getId())
                            .levelId(profile.getLevelId())
                            .gradeId(profile.getGradeId())
                            .sectionId(profile.getSectionId())
                            .build());
                    generated++;
                }
                cursor = cursor.plusMonths(1);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("profileId", profileId);
        result.put("generated", generated);
        return result;
    }

    // ── F. List charges ───────────────────────────────────────────────────────

    public List<PaymentChargeResponse> listCharges(Long institutionId, Long academicYearId,
                                                    Long studentId) {
        List<PaymentCharge> charges;
        if (studentId != null && academicYearId != null) {
            charges = chargeRepo.findByInstitutionIdAndStudentIdAndAcademicYearIdOrderByDueDateAsc(
                    institutionId, studentId, academicYearId);
        } else if (studentId != null) {
            charges = chargeRepo.findByInstitutionIdAndStudentIdOrderByDueDateAsc(
                    institutionId, studentId);
        } else if (academicYearId != null) {
            charges = chargeRepo.findByInstitutionIdAndAcademicYearIdOrderByDueDateAsc(
                    institutionId, academicYearId);
        } else {
            charges = List.of();
        }

        // Batch-load scales for mora computation
        Map<Long, PaymentMonthlyScale> scaleMap = buildScaleMap(charges, institutionId);
        LocalDate today = LocalDate.now();

        return charges.stream()
                .map(c -> toChargeResponse(c, scaleMap.get(c.getMonthlyScaleId()), today))
                .toList();
    }

    // ── G. Mark charge as paid ────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> markPaid(Long chargeId, Long institutionId, PayChargeRequest req) {
        PaymentCharge charge = chargeRepo.findByIdAndInstitutionId(chargeId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentCharge", chargeId));

        if ("PAID".equals(charge.getStatus())) {
            throw new BusinessException("Charge already paid", HttpStatus.CONFLICT, "ALREADY_PAID");
        }

        charge.setStatus("PAID");
        charge.setPaidAt(req.getPaidAt() != null ? req.getPaidAt() : LocalDate.now());
        charge.setPaidAmount(req.getPaidAmount());
        chargeRepo.save(charge);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", charge.getId());
        result.put("status", "PAID");
        result.put("paidAt", charge.getPaidAt());
        result.put("paidAmount", charge.getPaidAmount());
        return result;
    }

    // ── H. Overview by level/grade/section ───────────────────────────────────

    public PaymentOverviewResponse getOverview(Long institutionId, Long academicYearId,
                                                Long levelId, Long gradeId, Long sectionId) {
        List<PaymentCharge> charges = academicYearId != null
                ? chargeRepo.findByInstitutionIdAndAcademicYearIdOrderByDueDateAsc(institutionId, academicYearId)
                : List.of();

        // Apply optional filters
        if (levelId != null) {
            charges = charges.stream().filter(c -> levelId.equals(c.getLevelId())).toList();
        }
        if (gradeId != null) {
            charges = charges.stream().filter(c -> gradeId.equals(c.getGradeId())).toList();
        }
        if (sectionId != null) {
            charges = charges.stream().filter(c -> sectionId.equals(c.getSectionId())).toList();
        }

        Map<Long, PaymentMonthlyScale> scaleMap = buildScaleMap(charges, institutionId);
        LocalDate today = LocalDate.now();

        // Compute effective responses
        List<PaymentChargeResponse> responses = charges.stream()
                .map(c -> toChargeResponse(c, scaleMap.get(c.getMonthlyScaleId()), today))
                .toList();

        // Build name maps for levels/grades/sections
        Set<Long> levelIds = charges.stream().map(PaymentCharge::getLevelId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> gradeIds = charges.stream().map(PaymentCharge::getGradeId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> sectionIds = charges.stream().map(PaymentCharge::getSectionId)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> levelNames = levelIds.isEmpty() ? Map.of()
                : levelRepo.findAllById(levelIds).stream()
                        .collect(Collectors.toMap(AcademicLevel::getId, AcademicLevel::getName));
        Map<Long, String> gradeNames = gradeIds.isEmpty() ? Map.of()
                : gradeRepo.findAllById(gradeIds).stream()
                        .collect(Collectors.toMap(AcademicGrade::getId, AcademicGrade::getName));
        Map<Long, String> sectionNames = sectionIds.isEmpty() ? Map.of()
                : sectionRepo.findAllById(sectionIds).stream()
                        .collect(Collectors.toMap(AcademicSection::getId, AcademicSection::getName));

        return PaymentOverviewResponse.builder()
                .byLevel(buildGroupSummaries(responses, charges, PaymentCharge::getLevelId, levelNames))
                .byGrade(buildGroupSummaries(responses, charges, PaymentCharge::getGradeId, gradeNames))
                .bySection(buildGroupSummaries(responses, charges, PaymentCharge::getSectionId, sectionNames))
                .build();
    }

    // ── I. Parent account-status ───────────────────────────────────────────────

    public AccountStatusResponse getAccountStatus(Long studentId, Long institutionId) {
        List<PaymentCharge> charges = chargeRepo
                .findByInstitutionIdAndStudentIdOrderByDueDateAsc(institutionId, studentId);

        Map<Long, PaymentMonthlyScale> scaleMap = buildScaleMap(charges, institutionId);
        LocalDate today = LocalDate.now();

        List<PaymentChargeResponse> chargeResponses = charges.stream()
                .map(c -> toChargeResponse(c, scaleMap.get(c.getMonthlyScaleId()), today))
                .toList();

        BigDecimal pendingAmount = chargeResponses.stream()
                .filter(r -> !"PAID".equals(r.getStatus()))
                .map(PaymentChargeResponse::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long overdueCount = chargeResponses.stream()
                .filter(r -> "OVERDUE".equals(r.getStatus())).count();
        long paidCount = chargeResponses.stream()
                .filter(r -> "PAID".equals(r.getStatus())).count();

        String studentName = userRepo.findById(studentId).map(User::getName).orElse(null);

        return AccountStatusResponse.builder()
                .studentId(studentId)
                .studentName(studentName)
                .pendingAmount(pendingAmount)
                .overdueCount(overdueCount)
                .paidCount(paidCount)
                .charges(chargeResponses)
                .build();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private PaymentChargeResponse toChargeResponse(PaymentCharge c, PaymentMonthlyScale scale,
                                                    LocalDate today) {
        BigDecimal lateAmt = computeLateAmount(c, scale, today);
        String effectiveStatus = computeStatus(c, today);

        return PaymentChargeResponse.builder()
                .id(c.getId())
                .studentId(c.getStudentId())
                .academicYearId(c.getAcademicYearId())
                .profileId(c.getProfileId())
                .kind(c.getKind())
                .label(c.getLabel())
                .monthKey(c.getMonthKey())
                .dueDate(c.getDueDate())
                .baseAmount(c.getBaseAmount())
                .lateAmount(lateAmt)
                .totalAmount(c.getBaseAmount().add(lateAmt))
                .status(effectiveStatus)
                .paidAt(c.getPaidAt())
                .paidAmount(c.getPaidAmount())
                .enrollmentTypeId(c.getEnrollmentTypeId())
                .monthlyScaleId(c.getMonthlyScaleId())
                .levelId(c.getLevelId())
                .gradeId(c.getGradeId())
                .sectionId(c.getSectionId())
                .build();
    }

    private BigDecimal computeLateAmount(PaymentCharge charge, PaymentMonthlyScale scale,
                                          LocalDate today) {
        // No mora for ENROLLMENT, no mora for paid charges
        if ("ENROLLMENT".equals(charge.getKind())) return BigDecimal.ZERO;
        if ("PAID".equals(charge.getStatus())) return BigDecimal.ZERO;
        if (scale == null || "NONE".equals(scale.getLateMode())) return BigDecimal.ZERO;
        if ("DAILY".equals(scale.getLateMode()) && scale.getLateAmount() != null) {
            long daysLate = ChronoUnit.DAYS.between(charge.getDueDate(), today);
            if (daysLate <= 0) return BigDecimal.ZERO;
            return scale.getLateAmount().multiply(BigDecimal.valueOf(daysLate));
        }
        return BigDecimal.ZERO;
    }

    private String computeStatus(PaymentCharge charge, LocalDate today) {
        if ("PAID".equals(charge.getStatus())) return "PAID";
        if (charge.getDueDate() != null && charge.getDueDate().isBefore(today)) return "OVERDUE";
        return "PENDING";
    }

    private Map<Long, PaymentMonthlyScale> buildScaleMap(List<PaymentCharge> charges,
                                                          Long institutionId) {
        Set<Long> scaleIds = charges.stream()
                .map(PaymentCharge::getMonthlyScaleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (scaleIds.isEmpty()) return Map.of();
        return monthlyScaleRepo.findAllById(scaleIds).stream()
                .collect(Collectors.toMap(PaymentMonthlyScale::getId, Function.identity()));
    }

    private List<PaymentOverviewResponse.GroupSummary> buildGroupSummaries(
            List<PaymentChargeResponse> responses,
            List<PaymentCharge> charges,
            Function<PaymentCharge, Long> keyExtractor,
            Map<Long, String> nameMap) {

        Map<Long, List<PaymentChargeResponse>> byKey = new LinkedHashMap<>();
        for (int i = 0; i < charges.size(); i++) {
            Long key = keyExtractor.apply(charges.get(i));
            if (key == null) continue;
            byKey.computeIfAbsent(key, k -> new ArrayList<>()).add(responses.get(i));
        }

        return byKey.entrySet().stream().map(entry -> {
            Long key = entry.getKey();
            List<PaymentChargeResponse> group = entry.getValue();
            BigDecimal pendingAmt = group.stream()
                    .filter(r -> !"PAID".equals(r.getStatus()))
                    .map(PaymentChargeResponse::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long pendingCount = group.stream()
                    .filter(r -> !"PAID".equals(r.getStatus())).count();
            long overdueCount = group.stream()
                    .filter(r -> "OVERDUE".equals(r.getStatus())).count();
            return PaymentOverviewResponse.GroupSummary.builder()
                    .key(String.valueOf(key))
                    .label(nameMap.getOrDefault(key, String.valueOf(key)))
                    .pendingCount(pendingCount)
                    .overdueCount(overdueCount)
                    .pendingAmount(pendingAmt)
                    .build();
        }).toList();
    }

    private static final String[] MESES_ES = {
        "Enero","Febrero","Marzo","Abril","Mayo","Junio",
        "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
    };

    private String buildMonthlyLabel(LocalDate date) {
        // "Mensualidad Mayo 2026" — hardcoded Spanish names, no JVM locale dependency
        String mes = MESES_ES[date.getMonthValue() - 1];
        return "Mensualidad " + mes + " " + date.getYear();
    }

    private EnrollmentTypeResponse toEnrollmentTypeResponse(PaymentEnrollmentType et) {
        return EnrollmentTypeResponse.builder()
                .id(et.getId())
                .institutionId(et.getInstitutionId())
                .name(et.getName())
                .description(et.getDescription())
                .amount(et.getAmount())
                .active(et.isActive())
                .createdAt(et.getCreatedAt())
                .build();
    }

    private MonthlyScaleResponse toMonthlyScaleResponse(PaymentMonthlyScale s) {
        return MonthlyScaleResponse.builder()
                .id(s.getId())
                .institutionId(s.getInstitutionId())
                .name(s.getName())
                .monthlyAmount(s.getMonthlyAmount())
                .lateMode(s.getLateMode())
                .lateAmount(s.getLateAmount())
                .active(s.isActive())
                .createdAt(s.getCreatedAt())
                .build();
    }

    private StudentPaymentProfileResponse toProfileResponse(PaymentStudentProfile p) {
        return StudentPaymentProfileResponse.builder()
                .id(p.getId())
                .institutionId(p.getInstitutionId())
                .studentId(p.getStudentId())
                .studentName(p.getStudentName())
                .studentDni(p.getStudentDni())
                .academicYearId(p.getAcademicYearId())
                .academicYearName(p.getAcademicYearName())
                .enrollmentTypeId(p.getEnrollmentTypeId())
                .monthlyScaleId(p.getMonthlyScaleId())
                .levelId(p.getLevelId())
                .gradeId(p.getGradeId())
                .sectionId(p.getSectionId())
                .levelName(p.getLevelName())
                .gradeName(p.getGradeName())
                .sectionName(p.getSectionName())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
