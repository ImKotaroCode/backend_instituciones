package backend_instituciones.backend_instituciones.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentGradesOverviewResponse {

    private Long studentId;
    private Long courseId;
    private Long periodId;
    private String periodName;
    private List<GradeItem> items;
    private BigDecimal currentAverageOver20;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeItem {
        private Long assessmentId;
        private String title;
        private String type;
        private BigDecimal weightPercentage;
        private BigDecimal maxScore;
        private BigDecimal score;              // null if not scored yet
        private String status;                 // APROBADO | DESAPROBADO | PENDIENTE
        private BigDecimal contributionOver20; // (score / maxScore) * (weightPercentage / 100) * 20
    }
}
