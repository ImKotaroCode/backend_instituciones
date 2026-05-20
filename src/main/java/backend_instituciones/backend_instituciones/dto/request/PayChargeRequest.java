package backend_instituciones.backend_instituciones.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PayChargeRequest {
    private LocalDate paidAt;
    private BigDecimal paidAmount;
}
