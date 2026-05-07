package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AcademicStatusRequest {
    @NotNull
    private Long academicYearId;

    @NotBlank
    private String status;

    private String observation;
}
