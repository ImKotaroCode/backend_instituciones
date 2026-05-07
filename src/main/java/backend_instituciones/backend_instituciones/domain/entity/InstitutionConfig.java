package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "institution_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InstitutionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false, unique = true)
    private Long institutionId;

    @Column(length = 200)
    private String name;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "primary_color", length = 7)
    private String primaryColor;

    @Column(name = "secondary_color", length = 7)
    private String secondaryColor;

    @Column(name = "background_image")
    private String backgroundImage;

    @Column(name = "font_family", length = 100)
    private String fontFamily;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
