package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_loans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseLoan {

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

    @Column(name = "requester_role", length = 30)
    private String requesterRole;

    @Column(name = "requester_id")
    private Long requesterId;

    @Column(name = "requester_name", length = 300)
    private String requesterName;

    @Column(name = "out_at")
    private LocalDateTime outAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "delivery_condition", length = 100)
    private String deliveryCondition;

    @Column(name = "return_condition", length = 100)
    private String returnCondition;

    @Column(name = "penalty_notes", columnDefinition = "TEXT")
    private String penaltyNotes;

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

    /** ENTREGADO | DEVUELTO | RETRASADO */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ENTREGADO";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
