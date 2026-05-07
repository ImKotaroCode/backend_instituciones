package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GuardianRequest {
    @NotNull
    private Long guardianId;

    @NotBlank
    private String relationship;

    private boolean isPrimaryContact;
    private boolean isBillingContact;
    private boolean isEmergencyContact;
    private boolean livesWithStudent;
}
