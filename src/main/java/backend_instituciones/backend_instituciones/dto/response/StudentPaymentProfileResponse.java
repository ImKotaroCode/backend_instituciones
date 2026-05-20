package backend_instituciones.backend_instituciones.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentPaymentProfileResponse {
    private Long id;
    private Long institutionId;
    private Long studentId;
    private String studentName;
    private String studentDni;
    private Long academicYearId;
    private String academicYearName;
    private Long enrollmentTypeId;
    private Long monthlyScaleId;
    private Long levelId;
    private Long gradeId;
    private Long sectionId;
    private String levelName;
    private String gradeName;
    private String sectionName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
