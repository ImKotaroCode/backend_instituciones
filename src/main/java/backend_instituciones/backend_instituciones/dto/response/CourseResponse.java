package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class CourseResponse {
    private Long id;
    private Long institutionId;
    private String name;
    private String description;
    private String area;
    private String status;
    private LocalDateTime createdAt;
}
