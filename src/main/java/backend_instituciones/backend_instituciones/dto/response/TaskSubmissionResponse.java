package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmissionResponse {

    private Long id;
    private Long taskId;
    private Long courseId;
    private int attemptNumber;
    private Long groupId;
    private String groupName;
    private Long studentId;
    private String studentName;
    private LocalDateTime submittedAt;
    /** PENDIENTE | ENVIADO | REVISADO | TARDIO */
    private String status;
    private String comment;
    private List<TaskSubmissionFileResponse> attachments;
    private BigDecimal score;
    private String feedback;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
