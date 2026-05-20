package backend_instituciones.backend_instituciones.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentTypeResponse {
    private Long id;
    private Long institutionId;
    private String name;
    private String description;
    private BigDecimal amount;
    private boolean active;
    private LocalDateTime createdAt;
}
