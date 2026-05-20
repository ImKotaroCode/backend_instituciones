package backend_instituciones.backend_instituciones.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOverviewResponse {

    private List<GroupSummary> byLevel;
    private List<GroupSummary> byGrade;
    private List<GroupSummary> bySection;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupSummary {
        private String key;           // levelId / gradeId / sectionId as string
        private String label;         // name
        private long pendingCount;    // PENDING + OVERDUE
        private long overdueCount;    // OVERDUE only
        private BigDecimal pendingAmount; // sum totalAmount for non-PAID
    }
}
