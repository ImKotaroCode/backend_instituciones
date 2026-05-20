package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AutoCheckInRequest {

    @NotNull
    private Long teacherId;

    @NotBlank
    private String triggeredAt;
}
