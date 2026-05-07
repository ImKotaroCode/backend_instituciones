package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseAssignmentRequest {
    @NotNull private Long academicYearId;
    @NotNull private Long levelId;
    @NotNull private Long gradeId;
    @NotNull private Long sectionId;
    @NotNull private Long courseCatalogId;
    @NotNull private Long teacherId;
}
