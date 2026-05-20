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
    private String kind;
    private String title;
    private String content;
    private String linkUrl;
    private String mediaUrl;
    private String mediaType;
    private String mediaName;
    private List<String> targetRoles;
    private Priority priority;
    private String monthKey;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
    private Long createdBy;
    /** Populated only in inbox responses — null if not seen */
    private LocalDateTime seenAt;
}
