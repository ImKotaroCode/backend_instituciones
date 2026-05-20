package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.SectionScheduleSlot;
import backend_instituciones.backend_instituciones.domain.entity.StudentAttendanceRecord;
import backend_instituciones.backend_instituciones.domain.entity.TeacherAttendanceSession;
import backend_instituciones.backend_instituciones.dto.response.StudentAttendanceOverviewResponse;
import backend_instituciones.backend_instituciones.dto.response.TeacherAttendanceOverviewResponse;
import backend_instituciones.backend_instituciones.repository.SectionScheduleSlotRepository;
import backend_instituciones.backend_instituciones.repository.StudentAttendanceRecordRepository;
import backend_instituciones.backend_instituciones.repository.TeacherAttendanceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseAttendanceOverviewService {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    private final StudentAttendanceRecordRepository studentRecordRepo;
    private final TeacherAttendanceSessionRepository teacherSessionRepo;
    private final SectionScheduleSlotRepository slotRepo;

    // ── Estudiante ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public StudentAttendanceOverviewResponse getStudentOverview(
            Long institutionId,
            Long studentId,
            Long courseId,
            Long periodId,
            Long sectionId,
            LocalDate dateFrom,
            LocalDate dateTo) {

        // Load attendance records for this student + course in date range
        List<StudentAttendanceRecord> records = studentRecordRepo.findFiltered(
                institutionId, null, dateFrom, dateTo, studentId, courseId, sectionId, null);

        // Load schedule slots for this course (filter by sectionId if provided)
        List<SectionScheduleSlot> slots = sectionId != null
                ? slotRepo.findByInstitutionIdAndSectionId(institutionId, sectionId).stream()
                        .filter(s -> courseId.equals(s.getCourseId())).toList()
                : slotRepo.findByInstitutionIdAndCourseId(institutionId, courseId);

        // Collapse to latest record per logical key (studentId+courseId+date+startTime+endTime)
        // findFiltered orders by attendanceDate DESC, startTime ASC — we need max id per key
        Map<String, StudentAttendanceRecord> latestByKey = new LinkedHashMap<>();
        for (StudentAttendanceRecord r : records) {
            String key = r.getStudentId() + "|" + r.getCourseId() + "|"
                    + r.getAttendanceDate() + "|" + r.getStartTime() + "|" + r.getEndTime();
            latestByKey.merge(key, r, (existing, incoming) ->
                    incoming.getId() > existing.getId() ? incoming : existing);
        }
        List<StudentAttendanceRecord> collapsed = new ArrayList<>(latestByKey.values());

        // Build summary
        long present  = collapsed.stream().filter(r -> "PRESENT".equals(r.getStatus())).count();
        long late     = collapsed.stream().filter(r -> "LATE".equals(r.getStatus())).count();
        long absent   = collapsed.stream().filter(r -> "ABSENT".equals(r.getStatus())).count();
        long excused  = collapsed.stream().filter(r -> "EXCUSED".equals(r.getStatus())).count();

        List<StudentAttendanceOverviewResponse.RecordItem> recordItems = collapsed.stream()
                .map(r -> StudentAttendanceOverviewResponse.RecordItem.builder()
                        .id(r.getId())
                        .periodId(r.getPeriodId())
                        .periodName(r.getPeriodName())
                        .studentId(r.getStudentId())
                        .studentName(r.getStudentName())
                        .courseId(r.getCourseId())
                        .courseName(r.getCourseName())
                        .sectionId(r.getSectionId())
                        .date(r.getAttendanceDate())
                        .status(r.getStatus())
                        .startTime(r.getStartTime())
                        .endTime(r.getEndTime())
                        .build())
                .toList();

        List<StudentAttendanceOverviewResponse.SlotItem> slotItems = slots.stream()
                .map(s -> StudentAttendanceOverviewResponse.SlotItem.builder()
                        .id(s.getId())
                        .weekday(s.getWeekday())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .courseId(s.getCourseId())
                        .courseName(s.getCourseName())
                        .build())
                .toList();

        return StudentAttendanceOverviewResponse.builder()
                .studentId(studentId)
                .courseId(courseId)
                .periodId(periodId)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .records(recordItems)
                .slots(slotItems)
                .summary(StudentAttendanceOverviewResponse.Summary.builder()
                        .present(present)
                        .late(late)
                        .absent(absent)
                        .excused(excused)
                        .build())
                .build();
    }

    // ── Docente ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public TeacherAttendanceOverviewResponse getTeacherOverview(
            Long institutionId,
            Long teacherId,
            Long courseId,
            Long periodId,
            Long sectionId,
            LocalDate dateFrom,
            LocalDate dateTo) {

        // Load teacher sessions for this course in date range
        List<TeacherAttendanceSession> sessions = teacherSessionRepo.findFiltered(
                institutionId, null, dateFrom, dateTo, teacherId, courseId, sectionId, null, null);

        // Load schedule slots for this course (filter by sectionId if provided)
        List<SectionScheduleSlot> slots = sectionId != null
                ? slotRepo.findByInstitutionIdAndSectionId(institutionId, sectionId).stream()
                        .filter(s -> courseId.equals(s.getCourseId())).toList()
                : slotRepo.findByInstitutionIdAndCourseId(institutionId, courseId);

        // Build summary
        long present = sessions.stream().filter(s -> "PRESENT".equals(s.getTeacherStatus())).count();
        long late    = sessions.stream().filter(s -> "LATE".equals(s.getTeacherStatus())).count();
        long absent  = sessions.stream().filter(s -> "ABSENT".equals(s.getTeacherStatus())).count();
        int totalTardiness = sessions.stream().mapToInt(TeacherAttendanceSession::getTardinessMinutes).sum();

        List<TeacherAttendanceOverviewResponse.SessionItem> sessionItems = sessions.stream()
                .map(s -> TeacherAttendanceOverviewResponse.SessionItem.builder()
                        .id(s.getId())
                        .courseId(s.getCourseId())
                        .courseName(s.getCourseName())
                        .sectionId(s.getSectionId())
                        .teacherId(s.getTeacherId())
                        .teacherName(s.getTeacherName())
                        .teacherStatus(s.getTeacherStatus())
                        .date(s.getAttendanceDate())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .checkInTime(s.getCheckInTime())
                        .tardinessMinutes(s.getTardinessMinutes())
                        .build())
                .toList();

        List<TeacherAttendanceOverviewResponse.SlotItem> slotItems = slots.stream()
                .map(s -> TeacherAttendanceOverviewResponse.SlotItem.builder()
                        .id(s.getId())
                        .weekday(s.getWeekday())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .courseId(s.getCourseId())
                        .courseName(s.getCourseName())
                        .build())
                .toList();

        return TeacherAttendanceOverviewResponse.builder()
                .teacherId(teacherId)
                .courseId(courseId)
                .periodId(periodId)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .sessions(sessionItems)
                .slots(slotItems)
                .summary(TeacherAttendanceOverviewResponse.Summary.builder()
                        .present(present)
                        .late(late)
                        .absent(absent)
                        .totalTardinessMinutes(totalTardiness)
                        .build())
                .build();
    }

    // ── Teacher today-windows ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Map<String, Object> getTodayWindows(Long institutionId, Long teacherId, LocalDate date) {
        LocalTime now = ZonedDateTime.now(LIMA).toLocalTime();
        String weekday = DayOfWeek.from(date).name();

        // All slots for this teacher on today's weekday
        List<SectionScheduleSlot> todaySlots = slotRepo
                .findByInstitutionIdAndTeacherId(institutionId, teacherId).stream()
                .filter(s -> s.getWeekday().equalsIgnoreCase(weekday))
                .sorted(Comparator.comparing(SectionScheduleSlot::getStartTime))
                .toList();

        if (todaySlots.isEmpty()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("teacherId", teacherId);
            empty.put("date", date.toString());
            empty.put("items", List.of());
            return empty;
        }

        // Batch-load sessions for this teacher today
        Map<Long, TeacherAttendanceSession> sessionByCourseSection = new HashMap<>();
        teacherSessionRepo.findFiltered(institutionId, date, null, null, teacherId, null, null, null, null)
                .forEach(s -> sessionByCourseSection.put(compositeKey(s.getCourseId(), s.getSectionId()), s));

        // Compute consecutive groups: (courseId, sectionId) → sorted slots
        Map<Long, List<SectionScheduleSlot>> consecutiveGroups = todaySlots.stream()
                .collect(Collectors.groupingBy(
                        s -> compositeKey(s.getCourseId(), s.getSectionId()),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<Map<String, Object>> items = new ArrayList<>();
        for (SectionScheduleSlot slot : todaySlots) {
            Long groupKey = compositeKey(slot.getCourseId(), slot.getSectionId());
            List<SectionScheduleSlot> group = consecutiveGroups.get(groupKey);
            int consecutiveIndex = group.indexOf(slot) + 1;
            int consecutiveTotal = group.size();

            TeacherAttendanceSession session = sessionByCourseSection.get(groupKey);
            String windowState = computeWindowState(slot, session, now, date);
            boolean canCheckIn = "UPCOMING".equals(windowState) || "OPEN".equals(windowState);
            boolean canTakeStudentAttendance = "OPEN".equals(windowState);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("courseId", slot.getCourseId());
            item.put("courseName", slot.getCourseName());
            item.put("sectionId", slot.getSectionId());
            item.put("sectionName", slot.getClassroomName());
            item.put("weekday", slot.getWeekday());
            item.put("startTime", slot.getStartTime().toString());
            item.put("endTime", slot.getEndTime().toString());
            item.put("attendanceWindowState", windowState);
            item.put("canCheckIn", canCheckIn);
            item.put("canTakeStudentAttendance", canTakeStudentAttendance);
            item.put("opensAt", slot.getStartTime().toString());
            item.put("closesAt", slot.getEndTime().toString());
            item.put("consecutiveIndex", consecutiveIndex);
            item.put("consecutiveTotal", consecutiveTotal);

            if (session != null) {
                Map<String, Object> sessionMap = new LinkedHashMap<>();
                sessionMap.put("id", session.getId());
                sessionMap.put("checkInTime", session.getCheckInTime() != null
                        ? session.getCheckInTime().toString() : null);
                sessionMap.put("teacherStatus", session.getTeacherStatus());
                sessionMap.put("tardinessMinutes", session.getTardinessMinutes());
                item.put("session", sessionMap);
            } else {
                item.put("session", null);
            }

            items.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("teacherId", teacherId);
        result.put("date", date.toString());
        result.put("items", items);
        return result;
    }

    private String computeWindowState(SectionScheduleSlot slot, TeacherAttendanceSession session,
                                       LocalTime now, LocalDate date) {
        LocalDate today = ZonedDateTime.now(LIMA).toLocalDate();
        if (session != null) {
            // Detect invalid: checkInTime recorded after endTime + 30 min
            if (session.getCheckInTime() != null && slot.getEndTime() != null
                    && session.getCheckInTime().isAfter(slot.getEndTime().plusMinutes(30))) {
                return "RECORDED_INVALID";
            }
            return "RECORDED";
        }
        if (!date.equals(today)) {
            // Past date without session = closed
            return date.isBefore(today) ? "CLOSED" : "UPCOMING";
        }
        if (now.isBefore(slot.getStartTime())) return "UPCOMING";
        if (!now.isAfter(slot.getEndTime())) return "OPEN";
        return "CLOSED";
    }

    private long compositeKey(Long courseId, Long sectionId) {
        return courseId * 1_000_000L + (sectionId != null ? sectionId : 0L);
    }
}
