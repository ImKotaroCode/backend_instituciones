package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ClassroomResponse {
    private Long id;
    private Long institutionId;
    private String name;
    private Long gradeLevelId;
    private String gradeLevelName;
    private Long sectionId;
    private String sectionName;
    private String academicYear;
    private Long tutorTeacherId;
    private String tutorTeacherName;
    private Long academicYearId;
    private String academicYearName;
    private Integer capacity;
    private Long studentCount;
    private LocalDateTime createdAt;
}
