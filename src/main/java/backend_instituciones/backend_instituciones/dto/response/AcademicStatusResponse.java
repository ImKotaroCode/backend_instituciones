package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AcademicStatusResponse {
    private Long id;
    private Long studentId;
    private Long academicYearId;
    private String academicYearName;
    private Long enrollmentId;
    private String classroomName;
    private String status;
    private String observation;
    private LocalDateTime createdAt;
}
