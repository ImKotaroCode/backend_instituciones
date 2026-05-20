package backend_instituciones.backend_instituciones.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StudentGradesOverviewResponse {

    private Long studentId;
    private Long academicYearId;
    private List<PeriodSummary> periods;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PeriodSummary {
        private Long periodId;
        private String periodName;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal generalAverage;
        private int courseCount;
        private List<CourseSummary> courses;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CourseSummary {
        private Long courseId;
        private String courseName;
        private Long teacherId;
        private String teacherName;
        private Long sectionId;
        private String sectionName;
        private String levelName;
        private String gradeName;
        private BigDecimal currentAverage;
        private BigDecimal configuredWeight;
        private BigDecimal scoredWeight;
        private int scoredCount;
        private int totalCount;
    }
}
