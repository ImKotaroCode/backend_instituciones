package backend_instituciones.backend_instituciones.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatusResponse {
    private Long studentId;
    private String studentName;
    private BigDecimal pendingAmount;  // sum of totalAmount for PENDING + OVERDUE
    private long overdueCount;
    private long paidCount;
    private List<PaymentChargeResponse> charges;
}
