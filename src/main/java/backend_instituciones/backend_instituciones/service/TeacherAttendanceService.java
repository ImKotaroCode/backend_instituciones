package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.SectionScheduleSlot;
import backend_instituciones.backend_instituciones.domain.entity.StudentAttendanceRecord;
import backend_instituciones.backend_instituciones.domain.entity.TeacherAttendanceSession;
import backend_instituciones.backend_instituciones.domain.entity.User;
import backend_instituciones.backend_instituciones.dto.request.AutoCheckInRequest;
import backend_instituciones.backend_instituciones.dto.request.TeacherAttendanceSessionRequest;
import backend_instituciones.backend_instituciones.dto.response.AutoCheckInResponse;
import backend_instituciones.backend_instituciones.dto.response.TeacherAttendanceSessionResponse;
import backend_instituciones.backend_instituciones.dto.response.TeacherCalendarDayResponse;
import backend_instituciones.backend_instituciones.dto.response.TeacherMySummaryResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.EnrollmentRepository;
import org.springframework.http.HttpStatus;
import backend_instituciones.backend_instituciones.repository.SectionScheduleSlotRepository;
import backend_instituciones.backend_instituciones.repository.StudentAttendanceRecordRepository;
import backend_instituciones.backend_instituciones.repository.TeacherAttendanceSessionRepository;
import backend_instituciones.backend_instituciones.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherAttendanceService {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    private final TeacherAttendanceSessionRepository teacherAttendanceSessionRepository;
    private final StudentAttendanceRecordRepository studentAttendanceRecordRepository;
    private final SectionScheduleSlotRepository sectionScheduleSlotRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    // ─── GET /sessions ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TeacherAttendanceSessionResponse> getSessions(
            Long institutionId, LocalDate date, LocalDate dateFrom, LocalDate dateTo,
            Long teacherId, Long courseId, Long sectionId, Long levelId, Long gradeId) {

        List<TeacherAttendanceSession> sessions = teacherAttendanceSessionRepository.findFiltered(
                institutionId, date, dateFrom, dateTo, teacherId, courseId, sectionId, levelId, gradeId);

        if (sessions.isEmpty()) return List.of();

        // Batch load all student records for all sessions at once — avoids N+1
        List<Long> sessionIds = sessions.stream().map(TeacherAttendanceSession::getId).toList();
        Map<Long, List<StudentAttendanceRecord>> recordsBySession =
                studentAttendanceRecordRepository.findBySessionIdIn(sessionIds).stream()
                        .collect(Collectors.groupingBy(StudentAttendanceRecord::getSessionId));

        return sessions.stream()
                .map(s -> toResponse(s, recordsBySession.getOrDefault(s.getId(), List.of())))
                .toList();
    }

    // ─── GET /session (legacy single-session lookup) ──────────────────────────

    @Transactional(readOnly = true)
    public Optional<TeacherAttendanceSessionResponse> getSession(Long institutionId, Long teacherId,
                                                                  Long courseId, Long sectionId,
                                                                  LocalDate date) {
        return teacherAttendanceSessionRepository
                .findByInstitutionIdAndCourseIdAndSectionIdAndTeacherIdAndAttendanceDate(
                        institutionId, courseId, sectionId, teacherId, date)
                .map(session -> {
                    List<StudentAttendanceRecord> records =
                            studentAttendanceRecordRepository.findBySessionId(session.getId());
                    return toResponse(session, records);
                });
    }

    // ─── POST /session ────────────────────────────────────────────────────────

    public TeacherAttendanceSessionResponse createSession(Long institutionId,
                                                           TeacherAttendanceSessionRequest req) {
        LocalDate date = LocalDate.parse(req.getDate());
        LocalTime startTime = LocalTime.parse(req.getStartTime());
        LocalTime endTime = LocalTime.parse(req.getEndTime());
        validateAttendanceWindow(date, endTime);
        LocalTime checkInTime = req.getCheckInTime() != null && !req.getCheckInTime().isBlank()
                ? LocalTime.parse(req.getCheckInTime()) : null;

        int tardiness = computeTardiness(startTime, checkInTime);
        String status = resolveStatus(req.getTeacherStatus(), startTime, checkInTime);

        TeacherAttendanceSession session = teacherAttendanceSessionRepository
                .findByInstitutionIdAndCourseIdAndSectionIdAndTeacherIdAndAttendanceDate(
                        institutionId, req.getCourseId(), req.getSectionId(), req.getTeacherId(), date)
                .orElseGet(() -> TeacherAttendanceSession.builder()
                        .institutionId(institutionId)
                        .teacherId(req.getTeacherId())
                        .courseId(req.getCourseId())
                        .sectionId(req.getSectionId())
                        .attendanceDate(date)
                        .build());

        session.setTeacherName(req.getTeacherName());
        session.setCourseName(req.getCourseName());
        session.setLevelId(req.getLevelId());
        session.setGradeId(req.getGradeId());
        session.setStartTime(startTime);
        session.setEndTime(endTime);
        session.setCheckInTime(checkInTime);
        session.setTeacherStatus(status);
        session.setTardinessMinutes(tardiness);
        session = teacherAttendanceSessionRepository.save(session);

        studentAttendanceRecordRepository.deleteBySessionId(session.getId());
        List<StudentAttendanceRecord> records = saveStudentRecords(
                institutionId, session, req.getStudentStatusMap());

        return toResponse(session, records);
    }

    // ─── PUT /session/{sessionId} ─────────────────────────────────────────────

    public TeacherAttendanceSessionResponse updateSession(Long institutionId, Long sessionId,
                                                           TeacherAttendanceSessionRequest req) {
        TeacherAttendanceSession session = teacherAttendanceSessionRepository
                .findByIdAndInstitutionId(sessionId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("TeacherAttendanceSession", sessionId));

        LocalDate sessionDate = session.getAttendanceDate();
        if (req.getEndTime() != null && !req.getEndTime().isBlank()) {
            validateAttendanceWindow(sessionDate, LocalTime.parse(req.getEndTime()));
        } else if (session.getEndTime() != null) {
            validateAttendanceWindow(sessionDate, session.getEndTime());
        }

        if (req.getStartTime() != null && !req.getStartTime().isBlank()) {
            session.setStartTime(LocalTime.parse(req.getStartTime()));
        }
        if (req.getEndTime() != null && !req.getEndTime().isBlank()) {
            session.setEndTime(LocalTime.parse(req.getEndTime()));
        }
        LocalTime checkInTime = req.getCheckInTime() != null && !req.getCheckInTime().isBlank()
                ? LocalTime.parse(req.getCheckInTime()) : session.getCheckInTime();
        session.setCheckInTime(checkInTime);

        int tardiness = computeTardiness(session.getStartTime(), checkInTime);
        String status = resolveStatus(req.getTeacherStatus(), session.getStartTime(), checkInTime);
        session.setTeacherStatus(status);
        session.setTardinessMinutes(tardiness);

        if (req.getTeacherName() != null) session.setTeacherName(req.getTeacherName());
        if (req.getCourseName() != null) session.setCourseName(req.getCourseName());
        if (req.getLevelId() != null) session.setLevelId(req.getLevelId());
        if (req.getGradeId() != null) session.setGradeId(req.getGradeId());

        session = teacherAttendanceSessionRepository.save(session);

        studentAttendanceRecordRepository.deleteBySessionId(session.getId());
        List<StudentAttendanceRecord> records = saveStudentRecords(
                institutionId, session, req.getStudentStatusMap());

        return toResponse(session, records);
    }

    // ─── GET /my-summary ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public TeacherMySummaryResponse getMySummary(Long institutionId, Long teacherId,
                                                  LocalDate dateFrom, LocalDate dateTo) {
        List<TeacherAttendanceSession> sessions =
                teacherAttendanceSessionRepository
                        .findByInstitutionIdAndTeacherIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
                                institutionId, teacherId, dateFrom, dateTo);

        int present = 0, late = 0, absent = 0, totalTardiness = 0;
        for (TeacherAttendanceSession s : sessions) {
            switch (s.getTeacherStatus()) {
                case "PRESENT" -> present++;
                case "LATE" -> { late++; totalTardiness += s.getTardinessMinutes(); }
                case "ABSENT" -> absent++;
            }
        }
        return TeacherMySummaryResponse.builder()
                .presentCount(present)
                .lateCount(late)
                .absentCount(absent)
                .totalTardinessMinutes(totalTardiness)
                .build();
    }

    // ─── GET /my-calendar ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TeacherCalendarDayResponse> getMyCalendar(Long institutionId, Long teacherId,
                                                           String month) {
        YearMonth ym = YearMonth.parse(month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        List<TeacherAttendanceSession> sessions =
                teacherAttendanceSessionRepository
                        .findByInstitutionIdAndTeacherIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
                                institutionId, teacherId, from, to);

        // Aggregate by date: worst status wins (ABSENT > LATE > PRESENT)
        Map<LocalDate, TeacherAttendanceSession> byDate = sessions.stream()
                .collect(Collectors.toMap(
                        TeacherAttendanceSession::getAttendanceDate,
                        s -> s,
                        (a, b) -> worstStatus(a, b)));

        return byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> TeacherCalendarDayResponse.builder()
                        .date(e.getKey().toString())
                        .status(e.getValue().getTeacherStatus())
                        .tardinessMinutes(e.getValue().getTardinessMinutes())
                        .build())
                .toList();
    }

    // ─── auto-check-in ────────────────────────────────────────────────────────

    public AutoCheckInResponse autoCheckIn(Long institutionId, Long teacherId, String triggeredAt) {
        ZonedDateTime utcTime = ZonedDateTime.parse(triggeredAt).withZoneSameInstant(LIMA);
        LocalDate date = utcTime.toLocalDate();
        LocalTime currentTime = utcTime.toLocalTime();
        String weekday = DayOfWeek.from(date).name();

        List<SectionScheduleSlot> teacherSlots =
                sectionScheduleSlotRepository.findByInstitutionIdAndTeacherId(institutionId, teacherId);

        Optional<SectionScheduleSlot> matchingSlot = teacherSlots.stream()
                .filter(slot -> slot.getWeekday().equalsIgnoreCase(weekday))
                .filter(slot -> {
                    LocalTime slotStart = slot.getStartTime();
                    LocalTime toleratedEnd = slot.getEndTime().plusMinutes(10);
                    return !currentTime.isBefore(slotStart) && currentTime.isBefore(toleratedEnd);
                })
                .findFirst();

        if (matchingSlot.isEmpty()) {
            return AutoCheckInResponse.builder().matched(false).build();
        }

        SectionScheduleSlot slot = matchingSlot.get();
        String status = currentTime.isAfter(slot.getStartTime().plusMinutes(5)) ? "LATE" : "PRESENT";
        int tardiness = computeTardiness(slot.getStartTime(), currentTime);

        User teacher = userRepository.findById(teacherId).orElse(null);
        String teacherName = teacher != null ? teacher.getName() : null;

        TeacherAttendanceSession session = teacherAttendanceSessionRepository
                .findByInstitutionIdAndCourseIdAndSectionIdAndTeacherIdAndAttendanceDate(
                        institutionId, slot.getCourseId(), slot.getSectionId(), teacherId, date)
                .orElseGet(() -> {
                    TeacherAttendanceSession newSession = TeacherAttendanceSession.builder()
                            .institutionId(institutionId)
                            .teacherId(teacherId)
                            .teacherName(teacherName)
                            .courseId(slot.getCourseId())
                            .sectionId(slot.getSectionId())
                            .levelId(slot.getLevelId())
                            .gradeId(slot.getGradeId())
                            .attendanceDate(date)
                            .startTime(slot.getStartTime())
                            .endTime(slot.getEndTime())
                            .checkInTime(currentTime)
                            .teacherStatus(status)
                            .tardinessMinutes(tardiness)
                            .build();
                    return teacherAttendanceSessionRepository.save(newSession);
                });

        List<StudentAttendanceRecord> existingRecords =
                studentAttendanceRecordRepository.findBySessionId(session.getId());
        if (existingRecords.isEmpty()) {
            List<Long> enrolledStudentIds = enrollmentRepository.findByCourseId(slot.getCourseId())
                    .stream().map(e -> e.getStudentId()).toList();
            // Batch load all students at once — avoids N+1
            Map<Long, User> studentMap = userRepository.findAllById(enrolledStudentIds)
                    .stream().collect(Collectors.toMap(User::getId, u -> u));
            List<StudentAttendanceRecord> newRecords = enrolledStudentIds.stream()
                    .map(studentId -> {
                        User student = studentMap.get(studentId);
                        return StudentAttendanceRecord.builder()
                                .institutionId(institutionId)
                                .sessionId(session.getId())
                                .studentId(studentId)
                                .studentName(student != null ? student.getName() : null)
                                .teacherId(teacherId)
                                .teacherName(teacherName)
                                .courseId(slot.getCourseId())
                                .sectionId(slot.getSectionId())
                                .attendanceDate(date)
                                .startTime(slot.getStartTime())
                                .endTime(slot.getEndTime())
                                .status("PRESENT")
                                .build();
                    }).toList();
            studentAttendanceRecordRepository.saveAll(newRecords);
        }

        return AutoCheckInResponse.builder()
                .matched(true)
                .courseId(slot.getCourseId())
                .sectionId(slot.getSectionId())
                .sessionId(session.getId())
                .teacherStatus(session.getTeacherStatus())
                .startTime(slot.getStartTime().toString())
                .endTime(slot.getEndTime().toString())
                .build();
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private void validateAttendanceWindow(LocalDate date, LocalTime endTime) {
        LocalDate today = ZonedDateTime.now(LIMA).toLocalDate();
        if (!date.equals(today)) return; // only enforce for today's classes
        LocalTime now = ZonedDateTime.now(LIMA).toLocalTime();
        if (now.isAfter(endTime)) {
            throw new BusinessException(
                    "La ventana de asistencia para esta clase ya cerro",
                    HttpStatus.UNPROCESSABLE_ENTITY, "ATTENDANCE_WINDOW_CLOSED");
        }
    }

    public TeacherAttendanceSessionResponse toResponse(TeacherAttendanceSession session,
                                                        List<StudentAttendanceRecord> records) {
        return TeacherAttendanceSessionResponse.builder()
                .id(session.getId())
                .teacherId(session.getTeacherId())
                .teacherName(session.getTeacherName())
                .courseId(session.getCourseId())
                .courseName(session.getCourseName())
                .sectionId(session.getSectionId())
                .levelId(session.getLevelId())
                .gradeId(session.getGradeId())
                .date(session.getAttendanceDate() != null ? session.getAttendanceDate().toString() : null)
                .startTime(session.getStartTime() != null ? session.getStartTime().toString() : null)
                .endTime(session.getEndTime() != null ? session.getEndTime().toString() : null)
                .checkInTime(session.getCheckInTime() != null ? session.getCheckInTime().toString() : null)
                .teacherStatus(session.getTeacherStatus())
                .tardinessMinutes(session.getTardinessMinutes())
                .studentStatusMap(toStudentMap(records))
                .build();
    }

    public Map<String, String> toStudentMap(List<StudentAttendanceRecord> records) {
        return records.stream()
                .collect(Collectors.toMap(
                        r -> r.getStudentId().toString(),
                        StudentAttendanceRecord::getStatus));
    }

    private int computeTardiness(LocalTime startTime, LocalTime checkInTime) {
        if (checkInTime == null || startTime == null) return 0;
        int minutes = (int) java.time.temporal.ChronoUnit.MINUTES.between(startTime, checkInTime);
        return Math.max(0, minutes);
    }

    private String resolveStatus(String frontendStatus, LocalTime startTime, LocalTime checkInTime) {
        if (checkInTime == null) return frontendStatus != null ? frontendStatus : "ABSENT";
        if (checkInTime.isAfter(startTime)) return "LATE";
        return "PRESENT";
    }

    private TeacherAttendanceSession worstStatus(TeacherAttendanceSession a, TeacherAttendanceSession b) {
        int rankA = statusRank(a.getTeacherStatus());
        int rankB = statusRank(b.getTeacherStatus());
        return rankA >= rankB ? a : b;
    }

    private int statusRank(String status) {
        return switch (status) {
            case "ABSENT" -> 3;
            case "LATE" -> 2;
            case "PRESENT" -> 1;
            default -> 0;
        };
    }

    private List<StudentAttendanceRecord> saveStudentRecords(Long institutionId,
                                                              TeacherAttendanceSession session,
                                                              Map<String, String> studentStatusMap) {
        if (studentStatusMap == null || studentStatusMap.isEmpty()) {
            return List.of();
        }
        return studentStatusMap.entrySet().stream()
                .filter(entry -> entry.getKey() != null && !entry.getKey().isBlank())
                .map(entry -> {
                    StudentAttendanceRecord record = StudentAttendanceRecord.builder()
                            .institutionId(institutionId)
                            .sessionId(session.getId())
                            .studentId(Long.parseLong(entry.getKey().trim()))
                            .teacherId(session.getTeacherId())
                            .teacherName(session.getTeacherName())
                            .courseId(session.getCourseId())
                            .courseName(session.getCourseName())
                            .sectionId(session.getSectionId())
                            .attendanceDate(session.getAttendanceDate())
                            .startTime(session.getStartTime())
                            .endTime(session.getEndTime())
                            .status(entry.getValue())
                            .build();
                    return studentAttendanceRecordRepository.save(record);
                })
                .toList();
    }
}
