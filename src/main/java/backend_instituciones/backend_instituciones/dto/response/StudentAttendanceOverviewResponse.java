package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttendanceOverviewResponse {

    private Long studentId;
    private Long courseId;
    private Long periodId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<RecordItem> records;
    private List<SlotItem> slots;
    private Summary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordItem {
        private Long id;
        private Long periodId;
        private String periodName;
        private Long studentId;
        private String studentName;
        private Long courseId;
        private String courseName;
        private Long sectionId;
        private LocalDate date;
        private String status;
        private LocalTime startTime;
        private LocalTime endTime;
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
        private long excused;
    }
}
