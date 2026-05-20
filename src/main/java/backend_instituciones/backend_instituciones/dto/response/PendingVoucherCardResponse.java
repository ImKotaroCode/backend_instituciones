package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PendingVoucherCardResponse {
    private Long studentId;
    private String studentName;
    private String studentPhotoUrl;
    private String studentDni;
    private Long academicYearId;
    private int alertCount;
    private LocalDateTime latestSubmittedAt;
    private List<VoucherItem> vouchers;

    @Data
    @Builder
    public static class VoucherItem {
        private Long id;
        private Long chargeId;
        private String chargeLabel;
        private String imageUrl;
        private String imageName;
        private String status;
        private LocalDateTime submittedAt;
    }
}
