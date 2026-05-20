package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentSectionRequest {
    @NotNull private Long levelId;
    @NotNull private Long gradeId;
    @NotNull private Long sectionId;
}
