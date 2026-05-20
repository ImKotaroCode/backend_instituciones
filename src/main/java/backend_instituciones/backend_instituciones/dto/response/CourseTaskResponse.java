package backend_instituciones.backend_instituciones.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CourseTaskResponse {

    private Long id;
    private String generatedCode;
    private Long institutionId;
    private Long courseId;
    private Long periodId;
    private String periodName;
    private String title;
    private String description;
    private String category;
    private LocalDateTime visibleFrom;
    private LocalDateTime dueAt;
    private BigDecimal maxScore;
    private int maxAttempts;
    private boolean allowLateSubmission;

    @JsonProperty("isGroupTask")
    private boolean groupTask;

    private List<String> acceptedFormats;
    private List<TaskGroupResponse> groups;
    private Long createdBy;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
