package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_guardians", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_user_id", "guardian_user_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentGuardian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "student_user_id", nullable = false)
    private Long studentId;

    @Column(name = "guardian_user_id", nullable = false)
    private Long guardianId;

    @Column(nullable = false, length = 20)
    private String relationship;

    @Column(name = "is_primary_contact", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isPrimaryContact = false;

    @Column(name = "is_billing_contact", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isBillingContact = false;

    @Column(name = "is_emergency_contact", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isEmergencyContact = false;

    @Column(name = "lives_with_student", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean livesWithStudent = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
