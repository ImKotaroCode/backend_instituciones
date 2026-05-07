package backend_instituciones.backend_instituciones.domain.entity;

import backend_instituciones.backend_instituciones.domain.enums.ExportJobStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "export_jobs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "requested_by")
    private Long requestedBy;

    @Column(name = "report_type", length = 50)
    private String reportType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ExportJobStatus status;

    @Column(name = "file_url")
    private String fileUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
