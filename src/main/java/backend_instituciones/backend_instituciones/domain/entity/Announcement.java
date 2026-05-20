package backend_instituciones.backend_instituciones.domain.entity;

import backend_instituciones.backend_instituciones.domain.enums.Priority;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "announcements",
        indexes = {
                @Index(name = "idx_ann_institution_status", columnList = "institution_id, status"),
                @Index(name = "idx_ann_institution_month", columnList = "institution_id, status, month_key")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(nullable = false, length = 300)
    private String title;

    /** Optional legacy field */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** Comma-separated: "PADRE,ESTUDIANTE,DOCENTE" */
    @Column(name = "target_roles", columnDefinition = "TEXT")
    private String targetRoles;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Priority priority;

    /** Optional external link */
    @Column(name = "link_url", length = 1000)
    private String linkUrl;

    /** Public URL of uploaded media (WEBP for images) */
    @Column(name = "media_url", length = 1000)
    private String mediaUrl;

    /** IMAGE | VIDEO */
    @Column(name = "media_type", length = 10)
    private String mediaType;

    @Column(name = "media_name", length = 300)
    private String mediaName;

    /** "YYYY-MM" — for month filtering */
    @Column(name = "month_key", length = 7)
    private String monthKey;

    /** ANUNCIO | BANNER */
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'ANUNCIO'")
    @Builder.Default
    private String kind = "ANUNCIO";

    /** BORRADOR | PUBLICADO | ARCHIVADO */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "BORRADOR";

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
