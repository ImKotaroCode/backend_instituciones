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
public class CourseAssessmentResponse {

    private Long id;
    private Long institutionId;
    private Long courseId;
    private Long periodId;
    private String periodName;
    private String title;
    private String description;
    private String period;
    /** EXAMEN | QUIZ | PROYECTO | TAREA | PARTICIPACION */
    private String type;
    private BigDecimal maxScore;
    private BigDecimal passingScore;
    private BigDecimal weightPercentage;
    private Long createdBy;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
