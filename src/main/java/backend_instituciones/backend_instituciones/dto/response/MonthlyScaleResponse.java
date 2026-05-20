package backend_instituciones.backend_instituciones.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyScaleResponse {
    private Long id;
    private Long institutionId;
    private String name;
    private BigDecimal monthlyAmount;
    private String lateMode;
    private BigDecimal lateAmount;
    private boolean active;
    private LocalDateTime createdAt;
}
