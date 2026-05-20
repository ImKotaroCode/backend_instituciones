package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.*;
import backend_instituciones.backend_instituciones.domain.enums.Role;
import backend_instituciones.backend_instituciones.repository.AcademicPeriodRepository;
import backend_instituciones.backend_instituciones.dto.request.StudentAttendanceRecordRequest;
import backend_instituciones.backend_instituciones.dto.response.*;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.*;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceCenterService {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    private final StudentAttendanceRecordRepository studentAttendanceRecordRepository;
    private final TeacherAttendanceSessionRepository teacherAttendanceSessionRepository;
    private final SectionScheduleSlotRepository sectionScheduleSlotRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    // New deps for attendance-center search/profile endpoints
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final StudentSectionAssignmentRepository studentSectionAssignmentRepository;
    private final AcademicSectionRepository academicSectionRepository;

    private static final List<Role> ATTENDANCE_ROLES = List.of(Role.DOCENTE, Role.ESTUDIANTE);

    // ─── GET /records ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<StudentAttendanceRecordResponse> getRecords(
            Long institutionId, LocalDate date, LocalDate dateFrom, LocalDate dateTo,
            Long studentId, Long courseId, Long sectionId, Long teacherId) {

        List<StudentAttendanceRecord> records = studentAttendanceRecordRepository.findFiltered(
                institutionId, date, dateFrom, dateTo, studentId, courseId, sectionId, teacherId);
        return records.stream().map(this::toResponse).toList();
    }

    // ─── POST /bulk ───────────────────────────────────────────────────────────

    public List<StudentAttendanceRecordResponse> bulkCreate(Long institutionId,
                                                             List<StudentAttendanceRecordRequest> requests) {
        LocalDate today = ZonedDateTime.now(LIMA).toLocalDate();
        LocalTime now = ZonedDateTime.now(LIMA).toLocalTime();

        // Cache periods to avoid N+1 per request
        Map<LocalDate, AcademicPeriod> periodCache = new HashMap<>();

        return requests.stream().map(req -> {
            LocalDate date = LocalDate.parse(req.getDate());
            LocalTime startTime = req.getStartTime() != null && !req.getStartTime().isBlank()
                    ? LocalTime.parse(req.getStartTime()) : null;
            LocalTime endTime = req.getEndTime() != null && !req.getEndTime().isBlank()
                    ? LocalTime.parse(req.getEndTime()) : null;

            if (date.equals(today) && endTime != null && now.isAfter(endTime)) {
                throw new BusinessException(
                        "La ventana de asistencia para esta clase ya cerro",
                        HttpStatus.UNPROCESSABLE_ENTITY, "ATTENDANCE_WINDOW_CLOSED");
            }

            // Resolve periodId/periodName: use from request or derive by date
            Long periodId = req.getPeriodId();
            String periodName = req.getPeriodName();
            if (periodId == null) {
                AcademicPeriod period = periodCache.computeIfAbsent(date, d -> {
                    List<AcademicPeriod> periods = academicPeriodRepository
                            .findByInstitutionIdAndDate(institutionId, d);
                    return periods.isEmpty() ? null : periods.get(0);
                });
                if (period != null) {
                    periodId = period.getId();
                    if (periodName == null) periodName = period.getName();
                }
            }

            // Upsert: update existing record for same student+course+section+date+slot
            List<StudentAttendanceRecord> existing = startTime != null
                    ? studentAttendanceRecordRepository.findExisting(
                            institutionId, req.getStudentId(), req.getCourseId(),
                            req.getSectionId(), date, startTime)
                    : List.of();

            StudentAttendanceRecord record;
            if (!existing.isEmpty()) {
                record = existing.get(0);
                record.setStatus(req.getStatus());
                record.setPeriodId(periodId);
                record.setPeriodName(periodName);
                if (req.getStudentName() != null) record.setStudentName(req.getStudentName());
                if (req.getTeacherId() != null) record.setTeacherId(req.getTeacherId());
                if (req.getTeacherName() != null) record.setTeacherName(req.getTeacherName());
                if (endTime != null) record.setEndTime(endTime);
                for (int i = 1; i < existing.size(); i++) {
                    studentAttendanceRecordRepository.delete(existing.get(i));
                }
            } else {
                record = StudentAttendanceRecord.builder()
                        .institutionId(institutionId)
                        .studentId(req.getStudentId())
                        .studentName(req.getStudentName())
                        .teacherId(req.getTeacherId())
                        .teacherName(req.getTeacherName())
                        .courseId(req.getCourseId())
                        .courseName(req.getCourseName())
                        .sectionId(req.getSectionId())
                        .attendanceDate(date)
                        .startTime(startTime)
                        .endTime(endTime)
                        .status(req.getStatus())
                        .periodId(periodId)
                        .periodName(periodName)
                        .build();
            }

            return toResponse(studentAttendanceRecordRepository.save(record));
        }).toList();
    }

    // ─── GET /alerts ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AttendanceAlertResponse> getAlerts(Long institutionId,
                                                    LocalDate date,
                                                    Long levelId, Long gradeId,
                                                    Long sectionId, Long teacherId) {
        ZonedDateTime nowLima = ZonedDateTime.now(LIMA);
        LocalDate today = date != null ? date : nowLima.toLocalDate();
        LocalTime nowTime = nowLima.toLocalTime();

        String weekday = today.getDayOfWeek().name();

        // Filtered at DB level — no full table scan
        List<SectionScheduleSlot> slots = sectionScheduleSlotRepository.findForAlerts(
                institutionId, weekday, levelId, gradeId, sectionId, teacherId);

        Map<Long, String> sectionNames = sectionRepository.findByInstitutionIdOrderByNameAsc(institutionId)
                .stream().collect(Collectors.toMap(Section::getId, Section::getName));

        List<AttendanceAlertResponse> alerts = new ArrayList<>();

        for (SectionScheduleSlot slot : slots) {

            long minutesElapsed = ChronoUnit.MINUTES.between(slot.getStartTime(), nowTime);
            if (minutesElapsed < 30) continue;

            Optional<TeacherAttendanceSession> session = teacherAttendanceSessionRepository
                    .findByInstitutionIdAndCourseIdAndSectionIdAndTeacherIdAndAttendanceDate(
                            institutionId, slot.getCourseId(), slot.getSectionId(),
                            slot.getTeacherId(), today);

            if (session.isPresent()) continue;

            User teacher = userRepository.findById(slot.getTeacherId()).orElse(null);
            String teacherName = teacher != null ? teacher.getName() : "DESCONOCIDO";
            String sectionName = sectionNames.getOrDefault(slot.getSectionId(), "");

            alerts.add(AttendanceAlertResponse.builder()
                    .id("alert-" + slot.getId() + "-" + today)
                    .teacherId(slot.getTeacherId())
                    .teacherName(teacherName)
                    .courseId(slot.getCourseId())
                    .courseName(slot.getCourseName() != null ? slot.getCourseName() : "")
                    .sectionId(slot.getSectionId())
                    .sectionName(sectionName)
                    .startTime(slot.getStartTime().toString())
                    .minutesLate((int) minutesElapsed)
                    .type("NO_CHECKIN_OVER_30")
                    .build());
        }

        return alerts;
    }

    // ─── GET /person-summary ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PersonSummaryResponse getPersonSummary(Long institutionId,
                                                   String personType, Long personId,
                                                   LocalDate dateFrom, LocalDate dateTo) {
        if ("TEACHER".equalsIgnoreCase(personType)) {
            List<TeacherAttendanceSession> sessions =
                    teacherAttendanceSessionRepository
                            .findByInstitutionIdAndTeacherIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
                                    institutionId, personId, dateFrom, dateTo);
            int present = 0, late = 0, absent = 0, totalTardiness = 0;
            for (TeacherAttendanceSession s : sessions) {
                switch (s.getTeacherStatus()) {
                    case "PRESENT" -> present++;
                    case "LATE" -> { late++; totalTardiness += s.getTardinessMinutes(); }
                    case "ABSENT" -> absent++;
                }
            }
            return PersonSummaryResponse.builder()
                    .personType("TEACHER")
                    .personId(personId)
                    .presentCount(present)
                    .lateCount(late)
                    .absentCount(absent)
                    .totalTardinessMinutes(totalTardiness)
                    .build();
        } else {
            List<StudentAttendanceRecord> records =
                    studentAttendanceRecordRepository
                            .findByInstitutionIdAndStudentIdAndAttendanceDateBetween(
                                    institutionId, personId, dateFrom, dateTo);
            int present = 0, late = 0, absent = 0, excused = 0;
            for (StudentAttendanceRecord r : records) {
                switch (r.getStatus()) {
                    case "PRESENT" -> present++;
                    case "LATE" -> late++;
                    case "ABSENT" -> absent++;
                    case "EXCUSED" -> excused++;
                }
            }
            return PersonSummaryResponse.builder()
                    .personType("STUDENT")
                    .personId(personId)
                    .presentCount(present)
                    .lateCount(late)
                    .absentCount(absent)
                    .excusedCount(excused)
                    .build();
        }
    }

    // ─── Search (attendance center) ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AttendanceCenterSearchResponse searchPersons(Long institutionId, String query, int size) {
        String q = query == null ? "" : query.trim();
        List<User> users = userRepository.searchByRolesAndQuery(
                institutionId, ATTENDANCE_ROLES, q,
                PageRequest.of(0, Math.min(size, 50)));

        List<AttendanceCenterSearchResponse.PersonItem> items = users.stream()
                .map(u -> AttendanceCenterSearchResponse.PersonItem.builder()
                        .id(u.getId())
                        .role(u.getRole().name())
                        .name(u.getName())
                        .email(u.getEmail())
                        .documentNumber(u.getDocumentNumber())
                        .active(u.isActive())
                        .build())
                .toList();

        return AttendanceCenterSearchResponse.builder()
                .query(q)
                .total(items.size())
                .items(items)
                .build();
    }

    // ─── Teacher profile (attendance center) ───────────────────────────────────

    @Transactional(readOnly = true)
    public AttendanceCenterTeacherResponse getTeacherProfile(
            Long institutionId, Long teacherId,
            LocalDate weekStart, LocalDate weekEnd, Long filterCourseId) {

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", teacherId));

        // Assignments for this teacher
        List<CourseAssignment> assignments = courseAssignmentRepository
                .findByTeacherIdAndInstitutionId(teacherId, institutionId);
        if (filterCourseId != null) {
            assignments = assignments.stream()
                    .filter(a -> filterCourseId.equals(a.getId())).toList();
        }

        // Section name lookup for assignments missing denormalized sectionName
        Set<Long> secIds = assignments.stream()
                .map(CourseAssignment::getSectionId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> secNames = secIds.isEmpty() ? Map.of()
                : academicSectionRepository.findAllById(secIds).stream()
                        .collect(Collectors.toMap(AcademicSection::getId, AcademicSection::getName));

        List<AttendanceCenterTeacherResponse.CourseInfo> courses = assignments.stream()
                .map(a -> AttendanceCenterTeacherResponse.CourseInfo.builder()
                        .id(a.getId()).name(a.getCourseName()).sectionId(a.getSectionId())
                        .sectionName(a.getSectionName() != null
                                ? a.getSectionName() : secNames.get(a.getSectionId()))
                        .build())
                .toList();

        // Schedule slots
        List<SectionScheduleSlot> slots = sectionScheduleSlotRepository
                .findByInstitutionIdAndTeacherId(institutionId, teacherId);
        if (filterCourseId != null) {
            slots = slots.stream()
                    .filter(s -> filterCourseId.equals(s.getCourseId())).toList();
        }

        // Sessions in range
        List<TeacherAttendanceSession> sessions = teacherAttendanceSessionRepository.findFiltered(
                institutionId, null, weekStart, weekEnd, teacherId, filterCourseId, null, null, null);

        Set<String> sessionKeys = sessions.stream()
                .map(s -> s.getCourseId() + "|" + s.getAttendanceDate() + "|" + s.getStartTime())
                .collect(Collectors.toSet());

        List<AttendanceCenterTeacherResponse.AlertInfo> alerts =
                buildTeacherAlerts(slots, sessionKeys, weekStart, weekEnd);

        long markings = sessions.stream().filter(s -> "PRESENT".equals(s.getTeacherStatus())).count();
        long late     = sessions.stream().filter(s -> "LATE".equals(s.getTeacherStatus())).count();
        int totalMins = sessions.stream().mapToInt(TeacherAttendanceSession::getTardinessMinutes).sum();

        return AttendanceCenterTeacherResponse.builder()
                .person(AttendanceCenterTeacherResponse.PersonInfo.builder()
                        .id(teacher.getId()).role(teacher.getRole().name())
                        .name(teacher.getName()).email(teacher.getEmail())
                        .documentNumber(teacher.getDocumentNumber()).build())
                .courses(courses)
                .summary(AttendanceCenterTeacherResponse.TeacherSummary.builder()
                        .markings(markings).late(late).alerts(alerts.size()).minutesLate(totalMins).build())
                .slots(slots.stream().map(s -> AttendanceCenterTeacherResponse.SlotInfo.builder()
                        .id(s.getId()).courseId(s.getCourseId()).courseName(s.getCourseName())
                        .sectionId(s.getSectionId()).weekday(s.getWeekday())
                        .startTime(s.getStartTime()).endTime(s.getEndTime()).build()).toList())
                .sessions(sessions.stream().map(s -> AttendanceCenterTeacherResponse.SessionInfo.builder()
                        .id(s.getId()).date(s.getAttendanceDate()).courseId(s.getCourseId())
                        .courseName(s.getCourseName()).startTime(s.getStartTime()).endTime(s.getEndTime())
                        .teacherStatus(s.getTeacherStatus()).checkInTime(s.getCheckInTime())
                        .tardinessMinutes(s.getTardinessMinutes()).build()).toList())
                .alerts(alerts)
                .build();
    }

    // ─── Student profile (attendance center) ───────────────────────────────────

    @Transactional(readOnly = true)
    public AttendanceCenterStudentResponse getStudentProfile(
            Long institutionId, Long studentId,
            LocalDate weekStart, LocalDate weekEnd, Long filterCourseId) {

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        Optional<StudentSectionAssignment> ssaOpt =
                studentSectionAssignmentRepository.findByInstitutionIdAndStudentId(institutionId, studentId);

        List<CourseAssignment> assignments = List.of();
        List<SectionScheduleSlot> slots = List.of();
        Long resolvedSectionId = null;

        if (ssaOpt.isPresent()) {
            StudentSectionAssignment ssa = ssaOpt.get();
            resolvedSectionId = ssa.getSectionId();

            List<Classroom> classrooms = classroomRepository
                    .findByInstitutionIdAndAcademicLevelIdAndAcademicGradeIdAndAcademicSectionId(
                            institutionId, ssa.getLevelId(), ssa.getGradeId(), ssa.getSectionId());

            List<Long> classroomIds = classrooms.stream().map(Classroom::getId).toList();
            if (!classroomIds.isEmpty()) {
                assignments = courseAssignmentRepository.findByClassroomIdInAndInstitutionId(classroomIds, institutionId);
            }
            slots = sectionScheduleSlotRepository.findByInstitutionIdAndSectionId(institutionId, ssa.getSectionId());
        }

        final Long sectionId = resolvedSectionId;
        if (filterCourseId != null) {
            assignments = assignments.stream().filter(a -> filterCourseId.equals(a.getId())).toList();
            slots = slots.stream().filter(s -> filterCourseId.equals(s.getCourseId())).toList();
        }

        Set<Long> secIds = assignments.stream()
                .map(CourseAssignment::getSectionId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (sectionId != null) secIds.add(sectionId);
        Map<Long, String> secNames = secIds.isEmpty() ? Map.of()
                : academicSectionRepository.findAllById(secIds).stream()
                        .collect(Collectors.toMap(AcademicSection::getId, AcademicSection::getName));

        List<AttendanceCenterStudentResponse.CourseInfo> courses = assignments.stream()
                .map(a -> {
                    Long sid = a.getSectionId() != null ? a.getSectionId() : sectionId;
                    return AttendanceCenterStudentResponse.CourseInfo.builder()
                            .id(a.getId()).name(a.getCourseName()).sectionId(sid)
                            .sectionName(a.getSectionName() != null
                                    ? a.getSectionName() : secNames.get(sid))
                            .build();
                })
                .toList();

        List<StudentAttendanceRecord> records = studentAttendanceRecordRepository.findFiltered(
                institutionId, null, weekStart, weekEnd, studentId, filterCourseId, sectionId, null);

        long markings = records.stream().filter(r -> "PRESENT".equals(r.getStatus())).count();
        long late     = records.stream().filter(r -> "LATE".equals(r.getStatus())).count();
        long absent   = records.stream().filter(r -> "ABSENT".equals(r.getStatus())).count();
        long excused  = records.stream().filter(r -> "EXCUSED".equals(r.getStatus())).count();

        return AttendanceCenterStudentResponse.builder()
                .person(AttendanceCenterStudentResponse.PersonInfo.builder()
                        .id(student.getId()).role(student.getRole().name())
                        .name(student.getName()).email(student.getEmail())
                        .documentNumber(student.getDocumentNumber()).build())
                .courses(courses)
                .summary(AttendanceCenterStudentResponse.StudentSummary.builder()
                        .markings(markings).late(late).absent(absent).excused(excused).build())
                .slots(slots.stream().map(s -> AttendanceCenterStudentResponse.SlotInfo.builder()
                        .id(s.getId()).courseId(s.getCourseId()).courseName(s.getCourseName())
                        .sectionId(s.getSectionId()).weekday(s.getWeekday())
                        .startTime(s.getStartTime()).endTime(s.getEndTime()).build()).toList())
                .records(records.stream().map(r -> AttendanceCenterStudentResponse.RecordInfo.builder()
                        .id(r.getId()).date(r.getAttendanceDate()).courseId(r.getCourseId())
                        .courseName(r.getCourseName()).startTime(r.getStartTime())
                        .endTime(r.getEndTime()).status(r.getStatus()).build()).toList())
                .build();
    }

    // ─── Alert builder ─────────────────────────────────────────────────────────

    private List<AttendanceCenterTeacherResponse.AlertInfo> buildTeacherAlerts(
            List<SectionScheduleSlot> slots, Set<String> sessionKeys,
            LocalDate weekStart, LocalDate weekEnd) {

        if (weekStart == null || weekEnd == null || slots.isEmpty()) return List.of();
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        List<AttendanceCenterTeacherResponse.AlertInfo> alerts = new ArrayList<>();

        for (SectionScheduleSlot slot : slots) {
            LocalDate d = weekStart;
            while (!d.isAfter(weekEnd)) {
                if (d.getDayOfWeek().name().equalsIgnoreCase(slot.getWeekday())) {
                    boolean isPast = d.isBefore(today)
                            || (d.isEqual(today) && slot.getEndTime() != null
                                && slot.getEndTime().isBefore(nowTime));
                    if (isPast) {
                        String key = slot.getCourseId() + "|" + d + "|" + slot.getStartTime();
                        if (!sessionKeys.contains(key)) {
                            alerts.add(AttendanceCenterTeacherResponse.AlertInfo.builder()
                                    .date(d).courseId(slot.getCourseId())
                                    .courseName(slot.getCourseName())
                                    .startTime(slot.getStartTime()).endTime(slot.getEndTime())
                                    .reason("NO_MARKING").build());
                        }
                    }
                }
                d = d.plusDays(1);
            }
        }
        alerts.sort(Comparator.comparing(AttendanceCenterTeacherResponse.AlertInfo::getDate));
        return alerts;
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    private StudentAttendanceRecordResponse toResponse(StudentAttendanceRecord r) {
        return StudentAttendanceRecordResponse.builder()
                .id(r.getId())
                .periodId(r.getPeriodId())
                .periodName(r.getPeriodName())
                .studentId(r.getStudentId())
                .studentName(r.getStudentName())
                .courseId(r.getCourseId())
                .courseName(r.getCourseName())
                .sectionId(r.getSectionId())
                .teacherId(r.getTeacherId())
                .teacherName(r.getTeacherName())
                .date(r.getAttendanceDate() != null ? r.getAttendanceDate().toString() : null)
                .startTime(r.getStartTime() != null ? r.getStartTime().toString() : null)
                .endTime(r.getEndTime() != null ? r.getEndTime().toString() : null)
                .status(r.getStatus())
                .build();
    }
}
