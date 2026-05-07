package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotBlank
    private String name;
    private String password;
}
