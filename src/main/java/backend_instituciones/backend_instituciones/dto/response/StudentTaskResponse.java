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
public class StudentTaskResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private String category;
    private LocalDateTime visibleFrom;
    private LocalDateTime dueAt;
    private BigDecimal maxScore;
    private boolean allowLateSubmission;

    @JsonProperty("isGroupTask")
    private boolean groupTask;

    private List<String> acceptedFormats;
    private String status;

    /** Grupo al que pertenece el alumno (si tarea grupal) */
    private MyGroupInfo myGroup;

    /** Entrega del alumno o su grupo */
    private SubmissionSummary submission;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyGroupInfo {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmissionSummary {
        private Long id;
        private String status;
        private BigDecimal score;
        private String feedback;
        private LocalDateTime submittedAt;
        private List<TaskSubmissionFileResponse> attachments;
    }
}
