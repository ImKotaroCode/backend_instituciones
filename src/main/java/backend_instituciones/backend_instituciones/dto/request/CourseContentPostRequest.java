package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseContentPostRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Long createdBy;

    private Long periodId;
    private String periodName;
}
