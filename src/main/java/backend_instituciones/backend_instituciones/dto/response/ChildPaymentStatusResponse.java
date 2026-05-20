package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChildPaymentStatusResponse {
    private Long studentId;
    private int pendingSubmissionCount;
    private int approvedCount;
    private int rejectedCount;
    private String latestStatus;
}
