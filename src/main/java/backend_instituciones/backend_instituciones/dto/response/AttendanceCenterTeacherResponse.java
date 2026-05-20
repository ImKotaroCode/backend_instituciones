package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCenterTeacherResponse {

    private PersonInfo person;
    private List<CourseInfo> courses;
    private TeacherSummary summary;
    private List<SlotInfo> slots;
    private List<SessionInfo> sessions;
    private List<AlertInfo> alerts;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PersonInfo {
        private Long id;
        private String role;
        private String name;
        private String email;
        private String documentNumber;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CourseInfo {
        private Long id;
        private String name;
        private Long sectionId;
        private String sectionName;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TeacherSummary {
        private long markings;
        private long late;
        private long alerts;
        private int minutesLate;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SlotInfo {
        private Long id;
        private Long courseId;
        private String courseName;
        private Long sectionId;
        private String weekday;
        private LocalTime startTime;
        private LocalTime endTime;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SessionInfo {
        private Long id;
        private LocalDate date;
        private Long courseId;
        private String courseName;
        private LocalTime startTime;
        private LocalTime endTime;
        private String teacherStatus;
        private LocalTime checkInTime;
        private int tardinessMinutes;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AlertInfo {
        private LocalDate date;
        private Long courseId;
        private String courseName;
        private LocalTime startTime;
        private LocalTime endTime;
        private String reason;
    }
}
