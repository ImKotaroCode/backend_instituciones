package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class GradeResponse {
    private Long id;
    private Long studentId;
    private Long courseId;
    private String period;
    private BigDecimal score;
    private String observations;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
