package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AcademicYearRequest {
    @NotBlank
    @Size(max = 20)
    private String name;

    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
}
