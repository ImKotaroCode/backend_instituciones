package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_maintenance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseMaintenance {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "asset_code", length = 50)
    private String assetCode;

    @Column(name = "asset_name", length = 300)
    private String assetName;

    /** PREVENTIVO | CORRECTIVO */
    @Column(nullable = false, length = 20)
    private String type;

    /** PENDIENTE | EN_PROCESO | COMPLETADO */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDIENTE";

    @Column(name = "scheduled_at")
    private LocalDate scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "provider_name", length = 300)
    private String providerName;

    @Column(name = "technician_name", length = 300)
    private String technicianName;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "warranty_until")
    private LocalDate warrantyUntil;

    @Column(name = "responsible_user_id")
    private Long responsibleUserId;

    @Column(name = "responsible_user_name", length = 300)
    private String responsibleUserName;

    @Column(name = "signed_by_user_id")
    private Long signedByUserId;

    @Column(name = "signed_by_user_name", length = 300)
    private String signedByUserName;

    @Column(name = "signature_url", columnDefinition = "TEXT")
    private String signatureUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
