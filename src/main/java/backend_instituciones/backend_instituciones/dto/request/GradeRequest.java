package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class GradeRequest {
    @NotNull
    private Long studentId;
    @NotNull
    private Long courseId;
    @NotBlank
    private String period;
    @NotNull
    @DecimalMin("0.00") @DecimalMax("20.00")
    private BigDecimal score;
    private String observations;
}
