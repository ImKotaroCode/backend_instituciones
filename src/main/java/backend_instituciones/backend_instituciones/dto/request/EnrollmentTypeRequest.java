package backend_instituciones.backend_instituciones.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EnrollmentTypeRequest {
    private String name;
    private String description;
    private BigDecimal amount;
    private boolean active = true;
}
