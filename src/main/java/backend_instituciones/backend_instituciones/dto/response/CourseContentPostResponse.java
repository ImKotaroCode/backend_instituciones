package backend_instituciones.backend_instituciones.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseContentPostResponse {
    private Long id;
    private Long courseId;
    private Long periodId;
    private String periodName;
    private String title;
    private String description;
    private LocalDateTime publishedAt;
    private Long createdBy;
    private String status;
    private List<CourseContentAttachmentResponse> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
