package backend_instituciones.backend_instituciones.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OnboardingRequest {

    @NotBlank
    @Email
    @JsonProperty("admin_email")
    private String adminEmail;
}
