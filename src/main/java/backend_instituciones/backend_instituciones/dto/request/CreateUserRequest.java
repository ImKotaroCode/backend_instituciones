package backend_instituciones.backend_instituciones.dto.request;

import backend_instituciones.backend_instituciones.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank
    private String name;
    @NotBlank @Email
    private String email;
    @NotNull
    private Role role;
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$",
             message = "Password must be 8+ chars with uppercase, number, symbol")
    private String password;
}
