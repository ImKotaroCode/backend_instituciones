package backend_instituciones.backend_instituciones.domain.entity;

import backend_instituciones.backend_instituciones.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"email", "institution_id"})
        },
        indexes = {
                @Index(name = "idx_users_institution_role", columnList = "institution_id, role"),
                @Index(name = "idx_users_institution_active", columnList = "institution_id, is_active")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "supabase_uid", unique = true, length = 36)
    private String supabaseUid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "alternative_phone", length = 20)
    private String alternativePhone;

    @Column(name = "document_type", length = 20)
    private String documentType;

    @Column(name = "document_number", length = 30)
    private String documentNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "address")
    private String address;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "must_complete_profile", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean mustCompleteProfile = false;

    @Column(name = "must_change_password", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean mustChangePassword = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
