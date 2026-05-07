package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GuardianResponse {
    private Long id;
    private Long studentId;
    private Long guardianId;
    private String guardianName;
    private String guardianEmail;
    private String guardianPhotoUrl;
    private String relationship;
    private boolean isPrimaryContact;
    private boolean isBillingContact;
    private boolean isEmergencyContact;
    private boolean livesWithStudent;
    private LocalDateTime createdAt;
}
