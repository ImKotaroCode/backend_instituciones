package backend_instituciones.backend_instituciones.dto.response;

import backend_instituciones.backend_instituciones.domain.enums.ExportJobStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class ExportJobResponse {
    private Long jobId;
    private ExportJobStatus status;
    private String fileUrl;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
