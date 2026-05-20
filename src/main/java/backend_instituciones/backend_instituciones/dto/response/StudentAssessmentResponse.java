package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAssessmentResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private String period;
    private String type;
    private BigDecimal maxScore;
    private BigDecimal passingScore;
    private LocalDateTime publishedAt;

    /** Nota del alumno en esta evaluacion. null si aun sin calificar */
    private ScoreSummary score;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreSummary {
        private BigDecimal value;
        /** APROBADO | DESAPROBADO | PENDIENTE */
        private String status;
        private String feedback;
    }
}
