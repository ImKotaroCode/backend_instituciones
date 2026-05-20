package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.*;
import backend_instituciones.backend_instituciones.domain.enums.Role;
import backend_instituciones.backend_instituciones.dto.response.*;
import backend_instituciones.backend_instituciones.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CourseAssignmentRepository assignmentRepo;
    private final CourseAssignmentService assignmentService;
    private final ClassroomRepository classroomRepo;
    private final SectionScheduleSlotRepository slotRepo;
    private final UserRepository userRepo;
    private final StudentSectionAssignmentRepository studentSectionRepo;
    private final CourseTaskRepository taskRepo;
    private final CourseTaskAllowedFormatRepository formatRepo;
    private final CourseTaskGroupRepository groupRepo;
    private final CourseTaskGroupMemberRepository memberRepo;
    private final CourseTaskSubmissionRepository submissionRepo;

    private final AcademicYearRepository academicYearRepo;
    private final AnnouncementRepository announcementRepo;
    private final PaymentChargeRepository paymentChargeRepo;
    private final PaymentVoucherSubmissionRepository voucherRepo;
    private final TeacherAttendanceSessionRepository teacherSessionRepo;
    private final StudentAttendanceRecordRepository studentRecordRepo;
    private final WarehouseAssetRepository warehouseAssetRepo;
    private final WarehouseLoanRepository warehouseLoanRepo;
    private final WarehouseMaintenanceRepository warehouseMaintenanceRepo;

    // ── Admin executive dashboard ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminDashboard(Long institutionId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        String monthKey = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // ── summary ──────────────────────────────────────────────────────────────
        long totalStudents    = userRepo.countByInstitutionIdAndRole(institutionId, Role.ESTUDIANTE);
        long totalTeachers    = userRepo.countByInstitutionIdAndRole(institutionId, Role.DOCENTE);
        long totalParents     = userRepo.countByInstitutionIdAndRole(institutionId, Role.PADRE);
        long totalWarehouse   = userRepo.countByInstitutionIdAndRole(institutionId, Role.ALMACEN);
        long totalAdmin       = userRepo.countByInstitutionIdAndRole(institutionId, Role.ADMINISTRACION);
        long totalCourses     = assignmentRepo.countByInstitutionId(institutionId);
        long publishedThisMonth = announcementRepo.countByInstitutionIdAndStatusAndMonthKey(
                institutionId, "PUBLICADO", monthKey);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalStudents", totalStudents);
        summary.put("totalTeachers", totalTeachers);
        summary.put("totalParents", totalParents);
        summary.put("totalWarehouseUsers", totalWarehouse);
        summary.put("totalAdministrationUsers", totalAdmin);
        summary.put("totalCourses", totalCourses);
        summary.put("publishedAnnouncementsThisMonth", publishedThisMonth);

        // ── payments ─────────────────────────────────────────────────────────────
        Optional<AcademicYear> activeYear = academicYearRepo.findByInstitutionIdAndIsCurrentTrue(institutionId);
        List<PaymentVoucherSubmission> submittedVouchers = activeYear.isPresent()
                ? voucherRepo.findByInstitutionIdAndAcademicYearIdAndStatusOrderBySubmittedAtDesc(
                        institutionId, activeYear.get().getId(), "SUBMITTED")
                : List.of();

        Map<String, Object> payments = new LinkedHashMap<>();
        if (activeYear.isPresent()) {
            AcademicYear ay = activeYear.get();
            BigDecimal pendingAmount = paymentChargeRepo.sumPendingAmount(institutionId, ay.getId());
            long pendingCount = paymentChargeRepo.countPending(institutionId, ay.getId());
            long overdueCount = paymentChargeRepo.countOverdue(institutionId, ay.getId(), today);
            payments.put("academicYearId", String.valueOf(ay.getId()));
            payments.put("academicYearName", ay.getName());
            payments.put("pendingAmount", pendingAmount);
            payments.put("pendingCount", pendingCount);
            payments.put("overdueCount", overdueCount);
            payments.put("validationAlertCount", submittedVouchers.size());
        } else {
            payments.put("academicYearId", null);
            payments.put("academicYearName", null);
            payments.put("pendingAmount", BigDecimal.ZERO);
            payments.put("pendingCount", 0);
            payments.put("overdueCount", 0);
            payments.put("validationAlertCount", 0);
        }

        // ── attendance ────────────────────────────────────────────────────────────
        long teacherSessionCount = teacherSessionRepo.countByInstitutionIdAndAttendanceDate(institutionId, today);
        long studentRecordCount  = studentRecordRepo.countByInstitutionIdAndAttendanceDate(institutionId, today);
        Map<String, Object> attendance = new LinkedHashMap<>();
        attendance.put("date", today.toString());
        attendance.put("teacherSessionCount", teacherSessionCount);
        attendance.put("studentRecordCount", studentRecordCount);

        // ── warehouse ─────────────────────────────────────────────────────────────
        long overdueCount = warehouseLoanRepo.countOverdue(institutionId, now);
        List<WarehouseLoan> overdueList = warehouseLoanRepo.findOverdue(institutionId, now, PageRequest.of(0, 8));
        long totalAssets        = warehouseAssetRepo.countByInstitutionId(institutionId);
        long lowStateAssets     = warehouseAssetRepo.countByInstitutionIdAndStatusIn(
                institutionId, List.of("MALO", "DETERIORADO"));
        long pendingMaintenance = warehouseMaintenanceRepo.countByInstitutionIdAndStatus(
                institutionId, "PENDIENTE");
        long criticalAlerts     = warehouseAssetRepo.countByInstitutionIdAndStatusIn(
                institutionId, List.of("BAJA"));

        Map<String, Object> warehouse = new LinkedHashMap<>();
        warehouse.put("totalAssets", totalAssets);
        warehouse.put("lowStateAssets", lowStateAssets);
        warehouse.put("overdueLoans", overdueCount);
        warehouse.put("pendingMaintenance", pendingMaintenance);
        warehouse.put("criticalAlerts", criticalAlerts);

        // ── announcements (published, this month, max 12) ─────────────────────────
        List<Announcement> rawAnnouncements = announcementRepo
                .findByInstitutionIdAndStatusAndMonthKeyOrderByPublishedAtDesc(
                        institutionId, "PUBLICADO", monthKey, PageRequest.of(0, 12));
        List<Map<String, Object>> announcements = rawAnnouncements.stream()
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", a.getId());
                    m.put("title", a.getTitle());
                    m.put("linkUrl", a.getLinkUrl());
                    m.put("targetRoles", a.getTargetRoles() != null
                            ? List.of(a.getTargetRoles().split(",")) : List.of());
                    m.put("priority", a.getPriority() != null ? a.getPriority().name() : null);
                    m.put("publishedAt", a.getPublishedAt());
                    m.put("createdAt", a.getCreatedAt());
                    return m;
                })
                .toList();

        // ── paymentAlerts (SUBMITTED vouchers grouped by student, max 8) ──────────
        List<Map<String, Object>> paymentAlerts = submittedVouchers.stream()
                .collect(Collectors.groupingBy(PaymentVoucherSubmission::getStudentId,
                        LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .limit(8)
                .map(e -> {
                    List<PaymentVoucherSubmission> vs = e.getValue();
                    PaymentVoucherSubmission first = vs.get(0);
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("studentId", String.valueOf(first.getStudentId()));
                    m.put("studentName", first.getStudentName());
                    m.put("studentPhotoUrl", first.getStudentPhotoUrl());
                    m.put("studentDni", first.getStudentDni());
                    m.put("academicYearId", activeYear.map(y -> String.valueOf(y.getId())).orElse(null));
                    m.put("alertCount", vs.size());
                    m.put("latestSubmittedAt", first.getSubmittedAt());
                    m.put("vouchers", List.of());
                    return m;
                })
                .toList();

        // ── warehouseAlerts (overdue loans, already limited to 8) ─────────────────
        List<Map<String, Object>> warehouseAlerts = overdueList.stream()
                .map(l -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", "loan-" + l.getId());
                    m.put("severity", "CRITICAL");
                    m.put("type", "PRESTAMO");
                    m.put("title", "Prestamo retrasado: " + (l.getAssetCode() != null ? l.getAssetCode() : l.getId()));
                    m.put("message", "Debio devolverse " + (l.getDueAt() != null ? l.getDueAt().toLocalDate() : "?"));
                    m.put("relatedId", String.valueOf(l.getId()));
                    m.put("dueAt", l.getDueAt() != null ? l.getDueAt().toLocalDate().toString() : null);
                    return m;
                })
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("payments", payments);
        result.put("attendance", attendance);
        result.put("warehouse", warehouse);
        result.put("announcements", announcements);
        result.put("paymentAlerts", paymentAlerts);
        result.put("warehouseAlerts", warehouseAlerts);
        return result;
    }

    // ── Teacher dashboard ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public TeacherDashboardResponse getTeacherDashboard(Long institutionId, Long teacherId) {
        // 1. Course assignments for teacher
        List<CourseAssignment> assignments =
                assignmentRepo.findByTeacherIdAndInstitutionId(teacherId, institutionId);

        List<CourseAssignmentResponse> courseResponses =
                assignmentService.toBatchResponsesPublic(assignments);

        List<TeacherDashboardResponse.DashboardCourseItem> courses = courseResponses.stream()
                .map(r -> TeacherDashboardResponse.DashboardCourseItem.builder()
                        .id(r.getId())
                        .name(r.getCourseName())
                        .academicYear(r.getAcademicYear())
                        .levelId(r.getLevelId())
                        .gradeId(r.getGradeId())
                        .sectionId(r.getSectionId())
                        .educationLevel(r.getEducationLevel())
                        .gradeNumber(r.getGrade())
                        .section(r.getSection())
                        .build())
                .toList();

        // 2. Unique sectionIds → batch load schedule
        Set<Long> sectionIds = courseResponses.stream()
                .map(CourseAssignmentResponse::getSectionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<SectionScheduleSlot> slots = sectionIds.isEmpty() ? List.of()
                : slotRepo.findByInstitutionIdAndSectionIdIn(institutionId, sectionIds);

        // 3. Batch load teacher names for slots
        Set<Long> teacherIds = slots.stream()
                .map(SectionScheduleSlot::getTeacherId).collect(Collectors.toSet());
        Map<Long, String> teacherNames = userRepo.findAllById(teacherIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        List<TeacherDashboardResponse.DashboardScheduleSlot> schedule = slots.stream()
                .map(s -> toTeacherSlot(s, teacherNames))
                .toList();

        return TeacherDashboardResponse.builder()
                .teacherId(teacherId)
                .courses(courses)
                .weeklySchedule(schedule)
                .build();
    }

    // ── Student dashboard ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public StudentDashboardResponse getStudentDashboard(Long institutionId, Long studentId) {
        // 1. Student section assignment
        Optional<StudentSectionAssignment> sectionAssignmentOpt =
                studentSectionRepo.findByInstitutionIdAndStudentId(institutionId, studentId);

        if (sectionAssignmentOpt.isEmpty()) {
            return StudentDashboardResponse.builder()
                    .studentId(studentId)
                    .courses(List.of())
                    .weeklySchedule(List.of())
                    .upcomingTasks(List.of())
                    .build();
        }

        StudentSectionAssignment sa = sectionAssignmentOpt.get();

        // 2. Classrooms for student's level/grade/section
        List<Classroom> classrooms = classroomRepo
                .findByInstitutionIdAndAcademicLevelIdAndAcademicGradeIdAndAcademicSectionId(
                        institutionId, sa.getLevelId(), sa.getGradeId(), sa.getSectionId());

        List<Long> classroomIds = classrooms.stream().map(Classroom::getId).toList();

        // 3. Course assignments for those classrooms
        List<CourseAssignment> assignments = classroomIds.isEmpty() ? List.of()
                : assignmentRepo.findByClassroomIdInAndInstitutionId(classroomIds, institutionId);

        List<CourseAssignmentResponse> courseResponses =
                assignmentService.toBatchResponsesPublic(assignments);

        List<StudentDashboardResponse.DashboardCourseItem> courses = courseResponses.stream()
                .map(r -> StudentDashboardResponse.DashboardCourseItem.builder()
                        .id(r.getId())
                        .name(r.getCourseName())
                        .academicYear(r.getAcademicYear())
                        .levelId(r.getLevelId())
                        .gradeId(r.getGradeId())
                        .sectionId(r.getSectionId())
                        .educationLevel(r.getEducationLevel())
                        .gradeNumber(r.getGrade())
                        .section(r.getSection())
                        .build())
                .toList();

        // 4. Schedule for student's section
        Set<Long> sectionIds = courseResponses.stream()
                .map(CourseAssignmentResponse::getSectionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<SectionScheduleSlot> slots = sectionIds.isEmpty() ? List.of()
                : slotRepo.findByInstitutionIdAndSectionIdIn(institutionId, sectionIds);

        Set<Long> teacherIds = slots.stream()
                .map(SectionScheduleSlot::getTeacherId).collect(Collectors.toSet());
        Map<Long, String> teacherNames = userRepo.findAllById(teacherIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        List<StudentDashboardResponse.DashboardScheduleSlot> schedule = slots.stream()
                .map(s -> toStudentSlot(s, teacherNames))
                .toList();

        // 5. Upcoming tasks — courseId in CourseTask = CourseAssignment PK
        List<Long> courseIds = assignments.stream()
                .map(CourseAssignment::getId)
                .distinct()
                .toList();

        List<StudentDashboardResponse.UpcomingTaskItem> upcoming = List.of();
        if (!courseIds.isEmpty()) {
            upcoming = buildUpcomingTasks(institutionId, studentId, courseIds, courseResponses);
        }

        return StudentDashboardResponse.builder()
                .studentId(studentId)
                .courses(courses)
                .weeklySchedule(schedule)
                .upcomingTasks(upcoming)
                .build();
    }

    // ── Upcoming-pending standalone endpoint ─────────────────────────────────────

    @Transactional(readOnly = true)
    public Map<String, Object> getUpcomingPending(Long institutionId, Long studentId,
                                                   boolean activeOnly, int limit) {
        Optional<StudentSectionAssignment> saOpt =
                studentSectionRepo.findByInstitutionIdAndStudentId(institutionId, studentId);
        if (saOpt.isEmpty()) {
            return Map.of("studentId", studentId, "total", 0, "items", List.of());
        }
        StudentSectionAssignment sa = saOpt.get();

        List<Classroom> classrooms = classroomRepo
                .findByInstitutionIdAndAcademicLevelIdAndAcademicGradeIdAndAcademicSectionId(
                        institutionId, sa.getLevelId(), sa.getGradeId(), sa.getSectionId());

        List<Long> classroomIds = classrooms.stream().map(Classroom::getId).toList();
        if (classroomIds.isEmpty()) return Map.of("studentId", studentId, "total", 0, "items", List.of());

        List<CourseAssignment> assignments =
                assignmentRepo.findByClassroomIdInAndInstitutionId(classroomIds, institutionId);
        // CourseTask.courseId = CourseAssignment PK (not catalog courseId)
        List<Long> courseIds = assignments.stream().map(CourseAssignment::getId).distinct().toList();
        if (courseIds.isEmpty()) return Map.of("studentId", studentId, "total", 0, "items", List.of());

        // teacher name per course (keyed by assignment PK)
        Map<Long, Long> teacherByCourse = assignments.stream()
                .collect(Collectors.toMap(CourseAssignment::getId,
                        CourseAssignment::getTeacherId, (a, b) -> a));
        Set<Long> teacherIds = new HashSet<>(teacherByCourse.values());
        Map<Long, User> teacherMap = userRepo.findAllById(teacherIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // section info
        Map<Long, Long> sectionByCourse = assignments.stream()
                .collect(Collectors.toMap(CourseAssignment::getId,
                        a -> {
                            Classroom c = classrooms.stream()
                                    .filter(cl -> cl.getId().equals(a.getClassroomId())).findFirst().orElse(null);
                            return c != null ? c.getAcademicSectionId() : 0L;
                        }, (x, y) -> x));

        // course name
        List<CourseAssignmentResponse> courseResponses = assignmentService.toBatchResponsesPublic(assignments);
        // keyed by assignment PK (= CourseTask.courseId)
        Map<Long, String> courseNameByCatalogId = courseResponses.stream()
                .collect(Collectors.toMap(CourseAssignmentResponse::getId,
                        CourseAssignmentResponse::getCourseName, (a, b) -> a));
        Map<Long, String> sectionNameByCourse = courseResponses.stream()
                .filter(r -> r.getSection() != null)
                .collect(Collectors.toMap(CourseAssignmentResponse::getId,
                        CourseAssignmentResponse::getSection, (a, b) -> a));

        LocalDateTime now = LocalDateTime.now();
        List<CourseTask> tasks = taskRepo.findVisiblePublishedTasks(institutionId, courseIds, now);

        // groups
        List<Long> taskIds = tasks.stream().map(CourseTask::getId).toList();
        Map<Long, List<CourseTaskGroup>> groupsMap = taskIds.isEmpty() ? Map.of()
                : groupRepo.findByTaskIdIn(taskIds).stream()
                        .collect(Collectors.groupingBy(CourseTaskGroup::getTaskId));
        List<Long> allGroupIds = groupsMap.values().stream()
                .flatMap(List::stream).map(CourseTaskGroup::getId).toList();
        Set<Long> myGroupIds = allGroupIds.isEmpty() ? Set.of()
                : memberRepo.findByGroupIdIn(allGroupIds).stream()
                        .filter(m -> m.getStudentId().equals(studentId))
                        .map(CourseTaskGroupMember::getGroupId)
                        .collect(Collectors.toSet());
        Map<Long, CourseTaskGroup> myGroupByTask = new HashMap<>();
        groupsMap.forEach((tid, groups) -> groups.forEach(g -> {
            if (myGroupIds.contains(g.getId())) myGroupByTask.put(tid, g);
        }));

        // submissions
        List<CourseTaskSubmission> allSubs = taskIds.isEmpty() ? List.of()
                : submissionRepo.findByTaskIdIn(taskIds).stream()
                        .filter(s -> studentId.equals(s.getStudentId())
                                || (s.getGroupId() != null && myGroupIds.contains(s.getGroupId())))
                        .toList();
        // count attempts per task
        Map<Long, Long> attemptsUsed = allSubs.stream()
                .collect(Collectors.groupingBy(CourseTaskSubmission::getTaskId, Collectors.counting()));
        // latest submission per task
        Map<Long, CourseTaskSubmission> latestSubByTask = allSubs.stream()
                .collect(Collectors.toMap(CourseTaskSubmission::getTaskId, Function.identity(), (a, b) -> a));

        // formats
        Map<Long, List<String>> formatsMap = taskIds.isEmpty() ? Map.of()
                : formatRepo.findByTaskIdIn(taskIds).stream()
                        .collect(Collectors.groupingBy(CourseTaskAllowedFormat::getTaskId,
                                Collectors.mapping(CourseTaskAllowedFormat::getExtension, Collectors.toList())));

        int effectiveLimit = limit > 0 ? limit : 20;

        List<Map<String, Object>> items = tasks.stream()
                .filter(t -> {
                    if (!activeOnly) return true;

                    // Exclude if deadline passed (regardless of allowLateSubmission)
                    if (t.getDueAt() != null && t.getDueAt().isBefore(now)) return false;

                    // Exclude if already reviewed/graded
                    boolean hasSubmission = latestSubByTask.containsKey(t.getId());
                    if (hasSubmission) {
                        CourseTaskSubmission sub = latestSubByTask.get(t.getId());
                        long used = attemptsUsed.getOrDefault(t.getId(), 0L);
                        if ("REVISADO".equals(sub.getStatus())) return false;
                        if (used >= t.getMaxAttempts()) return false;
                    }

                    return true;
                })
                .limit(effectiveLimit)
                .map(t -> {
                    CourseTaskGroup myGroup = myGroupByTask.get(t.getId());
                    long used = attemptsUsed.getOrDefault(t.getId(), 0L);
                    boolean hasSubmission = latestSubByTask.containsKey(t.getId());
                    String reason = !hasSubmission ? "NO_SUBMITTED"
                            : used < t.getMaxAttempts() ? "CAN_RETRY" : "MAX_ATTEMPTS";

                    Long teacherId = teacherByCourse.get(t.getCourseId());
                    User teacher = teacherId != null ? teacherMap.get(teacherId) : null;

                    Map<String, Object> item = new java.util.LinkedHashMap<>();
                    item.put("taskId", t.getId());
                    item.put("courseId", t.getCourseId());
                    item.put("courseName", courseNameByCatalogId.getOrDefault(t.getCourseId(), ""));
                    item.put("title", t.getTitle());
                    item.put("description", t.getDescription());
                    item.put("visibleFrom", t.getVisibleFrom());
                    item.put("dueAt", t.getDueAt());
                    item.put("status", t.getStatus());
                    item.put("maxScore", t.getMaxScore());
                    item.put("maxAttempts", t.getMaxAttempts());
                    item.put("attemptsUsed", used);
                    item.put("allowLateSubmission", t.isAllowLateSubmission());
                    item.put("isGroupTask", t.isGroupTask());
                    item.put("groupId", myGroup != null ? myGroup.getId() : null);
                    item.put("groupName", myGroup != null ? myGroup.getName() : null);
                    item.put("acceptedFormats", formatsMap.getOrDefault(t.getId(), List.of()));
                    item.put("periodId", t.getPeriodId());
                    item.put("periodName", t.getPeriodName());
                    item.put("teacherId", teacherId);
                    item.put("teacherName", teacher != null ? teacher.getName() : null);
                    item.put("sectionId", sectionByCourse.get(t.getCourseId()));
                    item.put("sectionName", sectionNameByCourse.getOrDefault(t.getCourseId(), null));
                    item.put("pendingReason", reason);
                    return item;
                })
                .toList();

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("studentId", studentId);
        result.put("total", items.size());
        result.put("items", items);
        return result;
    }

    // ── Upcoming tasks builder ───────────────────────────────────────────────────

    private List<StudentDashboardResponse.UpcomingTaskItem> buildUpcomingTasks(
            Long institutionId, Long studentId,
            List<Long> courseIds, List<CourseAssignmentResponse> courseResponses) {

        LocalDateTime now = LocalDateTime.now();

        // Visible published tasks for all student courses
        List<CourseTask> tasks = taskRepo.findVisiblePublishedTasks(institutionId, courseIds, now);
        if (tasks.isEmpty()) return List.of();

        // Limit to 20 (already sorted by dueAt asc from query)
        if (tasks.size() > 20) tasks = tasks.subList(0, 20);

        List<Long> taskIds = tasks.stream().map(CourseTask::getId).toList();

        // Batch load formats
        Map<Long, List<String>> formatsMap = formatRepo.findByTaskIdIn(taskIds).stream()
                .collect(Collectors.groupingBy(CourseTaskAllowedFormat::getTaskId,
                        Collectors.mapping(CourseTaskAllowedFormat::getExtension, Collectors.toList())));

        // Batch load groups for group tasks
        Map<Long, List<CourseTaskGroup>> groupsMap = groupRepo.findByTaskIdIn(taskIds).stream()
                .collect(Collectors.groupingBy(CourseTaskGroup::getTaskId));

        List<Long> allGroupIds = groupsMap.values().stream()
                .flatMap(List::stream).map(CourseTaskGroup::getId).toList();

        // Student's group memberships
        Set<Long> myGroupIds = allGroupIds.isEmpty() ? Set.of()
                : memberRepo.findByGroupIdIn(allGroupIds).stream()
                        .filter(m -> m.getStudentId().equals(studentId))
                        .map(CourseTaskGroupMember::getGroupId)
                        .collect(Collectors.toSet());

        Map<Long, CourseTaskGroup> myGroupByTask = new HashMap<>();
        groupsMap.forEach((tid, groups) -> groups.forEach(g -> {
            if (myGroupIds.contains(g.getId())) myGroupByTask.put(tid, g);
        }));

        // Batch load submissions (by student or by group)
        List<CourseTaskSubmission> allSubmissions = submissionRepo.findByTaskIdIn(taskIds).stream()
                .filter(s -> studentId.equals(s.getStudentId())
                        || (s.getGroupId() != null && myGroupIds.contains(s.getGroupId())))
                .toList();
        Map<Long, CourseTaskSubmission> submissionByTask = allSubmissions.stream()
                .collect(Collectors.toMap(CourseTaskSubmission::getTaskId, Function.identity(),
                        (a, b) -> a));

        // course name lookup by assignment ID (matches CourseTask.courseId = assignment PK)
        Map<Long, String> courseNameById = courseResponses.stream()
                .collect(Collectors.toMap(
                        CourseAssignmentResponse::getId,
                        CourseAssignmentResponse::getCourseName,
                        (a, b) -> a));

        return tasks.stream()
                .filter(t -> !submissionByTask.containsKey(t.getId())) // only pending
                .map(t -> {
                    CourseTaskGroup myGroup = myGroupByTask.get(t.getId());
                    CourseTaskSubmission sub = submissionByTask.get(t.getId());

                    return StudentDashboardResponse.UpcomingTaskItem.builder()
                            .courseId(t.getCourseId())
                            .courseName(courseNameById.getOrDefault(t.getCourseId(), ""))
                            .groupId(myGroup != null ? myGroup.getId() : null)
                            .groupName(myGroup != null ? myGroup.getName() : null)
                            .task(StudentDashboardResponse.UpcomingTaskItem.TaskSummary.builder()
                                    .id(t.getId())
                                    .title(t.getTitle())
                                    .description(t.getDescription())
                                    .category(t.getCategory())
                                    .visibleFrom(t.getVisibleFrom())
                                    .dueAt(t.getDueAt())
                                    .maxScore(t.getMaxScore())
                                    .acceptedFormats(formatsMap.getOrDefault(t.getId(), List.of()))
                                    .groupTask(t.isGroupTask())
                                    .allowLateSubmission(t.isAllowLateSubmission())
                                    .status(t.getStatus())
                                    .build())
                            .submission(sub != null
                                    ? StudentDashboardResponse.UpcomingTaskItem.SubmissionSummary.builder()
                                            .id(sub.getId())
                                            .status(sub.getStatus())
                                            .score(sub.getScore())
                                            .submittedAt(sub.getSubmittedAt())
                                            .build()
                                    : null)
                            .build();
                })
                .toList();
    }

    // ── Slot converters ──────────────────────────────────────────────────────────

    private TeacherDashboardResponse.DashboardScheduleSlot toTeacherSlot(
            SectionScheduleSlot s, Map<Long, String> teacherNames) {
        return TeacherDashboardResponse.DashboardScheduleSlot.builder()
                .id(s.getId())
                .sectionId(s.getSectionId())
                .gradeId(s.getGradeId())
                .levelId(s.getLevelId())
                .courseId(s.getCourseId())
                .courseName(s.getCourseName())
                .teacherId(s.getTeacherId())
                .teacherName(teacherNames.get(s.getTeacherId()))
                .weekday(s.getWeekday())
                .startTime(s.getStartTime() != null ? s.getStartTime().toString() : null)
                .endTime(s.getEndTime() != null ? s.getEndTime().toString() : null)
                .classroomName(s.getClassroomName())
                .build();
    }

    private StudentDashboardResponse.DashboardScheduleSlot toStudentSlot(
            SectionScheduleSlot s, Map<Long, String> teacherNames) {
        return StudentDashboardResponse.DashboardScheduleSlot.builder()
                .id(s.getId())
                .sectionId(s.getSectionId())
                .gradeId(s.getGradeId())
                .levelId(s.getLevelId())
                .courseId(s.getCourseId())
                .courseName(s.getCourseName())
                .teacherId(s.getTeacherId())
                .teacherName(teacherNames.get(s.getTeacherId()))
                .weekday(s.getWeekday())
                .startTime(s.getStartTime() != null ? s.getStartTime().toString() : null)
                .endTime(s.getEndTime() != null ? s.getEndTime().toString() : null)
                .classroomName(s.getClassroomName())
                .build();
    }
}
