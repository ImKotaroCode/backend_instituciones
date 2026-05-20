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
public class TeacherAttendanceOverviewResponse {

    private Long teacherId;
    private Long courseId;
    private Long periodId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<SessionItem> sessions;
    private List<SlotItem> slots;
    private Summary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionItem {
        private Long id;
        private Long courseId;
        private String courseName;
        private Long sectionId;
        private Long teacherId;
        private String teacherName;
        private String teacherStatus;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private LocalTime checkInTime;
        private int tardinessMinutes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotItem {
        private Long id;
        private String weekday;
        private LocalTime startTime;
        private LocalTime endTime;
        private Long courseId;
        private String courseName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private long present;
        private long late;
        private long absent;
        private int totalTardinessMinutes;
    }
}
