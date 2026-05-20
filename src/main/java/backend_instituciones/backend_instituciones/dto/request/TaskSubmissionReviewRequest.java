package backend_instituciones.backend_instituciones.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TaskSubmissionReviewRequest {

    private BigDecimal score;
    private String feedback;
    /** REVISADO */
    private String status;
}
