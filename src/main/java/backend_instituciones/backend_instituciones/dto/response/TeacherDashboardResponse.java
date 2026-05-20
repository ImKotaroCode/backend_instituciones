package backend_instituciones.backend_instituciones.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDashboardResponse {

    private Long teacherId;
    private List<DashboardCourseItem> courses;
    private List<DashboardScheduleSlot> weeklySchedule;

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
}
