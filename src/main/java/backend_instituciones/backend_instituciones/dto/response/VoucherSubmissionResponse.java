package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VoucherSubmissionResponse {
    private Long id;
    private Long chargeId;
    private Long studentId;
    private String studentName;
    private String studentPhotoUrl;
    private String studentDni;
    private Long academicYearId;
    private Long parentId;
    private String imageUrl;
    private String imageName;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
    private String reviewerName;
    private String reviewNote;
}
