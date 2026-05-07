package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_jobs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ImportJob {

    @Id
    private String id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "classroom_id")
    private Long classroomId;

    @Column(name = "academic_year", length = 10)
    private String academicYear;

    @Builder.Default
    private Integer processed = 0;

    @Builder.Default
    private Integer created = 0;

    @Builder.Default
    private Integer updated = 0;

    @Builder.Default
    private Integer failed = 0;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
