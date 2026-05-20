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
public class StudentDashboardResponse {

    private Long studentId;
    private List<DashboardCourseItem> courses;
    private List<DashboardScheduleSlot> weeklySchedule;
    private List<UpcomingTaskItem> upcomingTasks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardCourseItem {
        private Long id;
        private String name;
        private String academicYear;
        private Long levelId;
        private Long gradeId;
        private Long sectionId;
        private String educationLevel;
        private String gradeNumber;
        private String section;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardScheduleSlot {
        private Long id;
        private Long sectionId;
        private Long gradeId;
        private Long levelId;
        private Long courseId;
        private String courseName;
        private Long teacherId;
        private String teacherName;
        private String weekday;
        private String startTime;
        private String endTime;
        private String classroomName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingTaskItem {
        private Long courseId;
        private String courseName;
        private TaskSummary task;
        /** Grupo del alumno si tarea grupal */
        private Long groupId;
        private String groupName;
        /** Entrega del alumno o grupo. null si sin entregar */
        private SubmissionSummary submission;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TaskSummary {
            private Long id;
            private String title;
            private String description;
            private String category;
            private LocalDateTime visibleFrom;
            private LocalDateTime dueAt;
            private BigDecimal maxScore;
            private List<String> acceptedFormats;

            @JsonProperty("isGroupTask")
            private boolean groupTask;

            private boolean allowLateSubmission;
            private String status;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SubmissionSummary {
            private Long id;
            private String status;
            private BigDecimal score;
            private LocalDateTime submittedAt;
        }
    }
}
