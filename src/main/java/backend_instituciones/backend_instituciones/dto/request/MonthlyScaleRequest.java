package backend_instituciones.backend_instituciones.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlyScaleRequest {
    private String name;
    private BigDecimal monthlyAmount;
    private String lateMode = "NONE";   // NONE | DAILY
    private BigDecimal lateAmount;
    private boolean active = true;
}
