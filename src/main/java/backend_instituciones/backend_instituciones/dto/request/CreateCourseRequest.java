package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCourseRequest {
    @NotBlank
    private String name;
    private String description;
    private String area;
}
