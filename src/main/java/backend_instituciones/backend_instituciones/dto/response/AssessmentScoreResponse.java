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
public class AssessmentScoreResponse {

    private Long id;
    private Long assessmentId;
    private Long studentId;
    private String studentName;
    private BigDecimal value;
    /** APROBADO | DESAPROBADO | PENDIENTE */
    private String status;
    private String feedback;
    private LocalDateTime updatedAt;
}
