package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SectionRequest {
    @NotBlank
    @Size(max = 20)
    private String name;
}
