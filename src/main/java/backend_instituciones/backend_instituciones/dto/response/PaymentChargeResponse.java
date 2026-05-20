package backend_instituciones.backend_instituciones.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentChargeResponse {
    private Long id;
    private Long studentId;
    private Long academicYearId;
    private Long profileId;
    private String kind;           // ENROLLMENT | MONTHLY
    private String label;
    private String monthKey;       // null for ENROLLMENT, "2026-05" for MONTHLY
    private LocalDate dueDate;
    private BigDecimal baseAmount;
    private BigDecimal lateAmount; // computed at runtime
    private BigDecimal totalAmount;// baseAmount + lateAmount
    private String status;         // PENDING | PAID | OVERDUE (computed)
    private LocalDate paidAt;
    private BigDecimal paidAmount;
    private Long enrollmentTypeId;
    private Long monthlyScaleId;
    private Long levelId;
    private Long gradeId;
    private Long sectionId;
}
