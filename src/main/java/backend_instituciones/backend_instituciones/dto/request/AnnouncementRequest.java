package backend_instituciones.backend_instituciones.dto.request;

import backend_instituciones.backend_instituciones.domain.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AnnouncementRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    @NotNull
    private List<String> targetRoles;
    private LocalDateTime scheduledAt;
    @NotNull
    private Priority priority;
}
