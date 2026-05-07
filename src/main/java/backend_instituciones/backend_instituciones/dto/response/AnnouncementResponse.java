package backend_instituciones.backend_instituciones.dto.response;

import backend_instituciones.backend_instituciones.domain.enums.Priority;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class AnnouncementResponse {
    private Long id;
    private Long institutionId;
    private String title;
    private String content;
    private List<String> targetRoles;
    private Priority priority;
    private LocalDateTime scheduledAt;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private Long createdBy;
}
