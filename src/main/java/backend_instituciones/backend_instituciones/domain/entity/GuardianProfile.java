package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "guardian_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GuardianProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "occupation", length = 100)
    private String occupation;

    @Column(name = "workplace", length = 200)
    private String workplace;

    @Column(name = "billing_email", length = 200)
    private String billingEmail;
}
