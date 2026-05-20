package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseAssessmentRequest {

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String period;

    private Long periodId;
    private String periodName;

    /** EXAMEN | QUIZ | PROYECTO | TAREA | PARTICIPACION */
    @NotBlank
    private String type;

    private BigDecimal maxScore = BigDecimal.valueOf(20);

    private BigDecimal passingScore = BigDecimal.valueOf(11);

    private BigDecimal weightPercentage = BigDecimal.ZERO;
}
