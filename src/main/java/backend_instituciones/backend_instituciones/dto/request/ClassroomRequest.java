package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClassroomRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    private Long gradeLevelId;
    private Long sectionId;

    @NotBlank
    @Size(max = 10)
    private String academicYear;

    private Long tutorTeacherId;
    private Long academicYearId;
    private Integer capacity;
}
