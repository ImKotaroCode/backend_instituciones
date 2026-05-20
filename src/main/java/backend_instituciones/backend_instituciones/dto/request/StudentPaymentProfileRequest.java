package backend_instituciones.backend_instituciones.dto.request;

import lombok.Data;

@Data
public class StudentPaymentProfileRequest {
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
}
