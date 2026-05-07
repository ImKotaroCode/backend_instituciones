package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GradeLevelRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    private Integer orderIndex;

    @Size(max = 50)
    private String level;
}
