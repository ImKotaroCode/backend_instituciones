package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EnrollmentResponse {
    private Long id;
    private Long institutionId;
    private Long studentId;
    private String studentName;
    private Long classroomId;
    private String classroomName;
    private Long academicYearId;
    private String academicYearName;
    private LocalDate enrollmentDate;
    private String status;
    private LocalDateTime createdAt;
}
