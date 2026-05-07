package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GradeLevelResponse {
    private Long id;
    private Long institutionId;
    private String name;
    private Integer orderIndex;
    private String level;
    private LocalDateTime createdAt;
}
