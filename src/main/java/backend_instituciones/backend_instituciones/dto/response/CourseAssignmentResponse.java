package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CourseAssignmentResponse {
    private Long id;
    private Long institutionId;
    private String generatedCode;
    private String status;
    private Long classroomId;
    private String classroomName;
    private Long levelId;
    private Long gradeId;
    private Long sectionId;
    private String educationLevel;
    private String grade;
    private String section;
    private String academicYear;
    private Long courseCatalogId;
    private String courseName;
    private String courseArea;
    private Long teacherUserId;
    private String teacherName;
    private LocalDateTime createdAt;
}
