package backend_instituciones.backend_instituciones.dto.request;

import lombok.Data;

@Data
public class VoucherReviewRequest {
    /** APPROVED | REJECTED */
    private String status;
    private Long reviewedBy;
    private String reviewerName;
    private String note;
}
