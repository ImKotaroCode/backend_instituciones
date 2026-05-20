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
public class StudentCourseTaskOverviewResponse {

    private Long studentId;
    private Long courseId;
    private List<TaskOverviewItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskOverviewItem {
        private TaskSummary task;
        private int attemptsUsed;
        private String pendingReason;
        private LatestSubmissionSummary latestSubmission;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskSummary {
        private Long id;
        private Long courseId;
        private String title;
        private String description;
        private String category;
        private LocalDateTime visibleFrom;
        private LocalDateTime dueAt;
        private String status;
        private BigDecimal maxScore;
        private int maxAttempts;
        private boolean allowLateSubmission;

        @JsonProperty("isGroupTask")
        private boolean groupTask;

        private List<String> acceptedFormats;
        private Long periodId;
        private String periodName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LatestSubmissionSummary {
        private Long id;
        private Long taskId;
        private Long courseId;
        private int attemptNumber;
        private Long studentId;
        private String studentName;
        private LocalDateTime submittedAt;
        private String status;
        private BigDecimal score;
        private String feedback;
    }
}
