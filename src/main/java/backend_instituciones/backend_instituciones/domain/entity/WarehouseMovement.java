package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_movements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseMovement {

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

    /** COMPRA | DONACION | TRASLADO | PRESTAMO | DEVOLUCION | ASIGNACION | BAJA */
    @Column(nullable = false, length = 30)
    private String type;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    @Column(name = "origin_room_id")
    private Long originRoomId;

    @Column(name = "origin_label", length = 300)
    private String originLabel;

    @Column(name = "destination_room_id")
    private Long destinationRoomId;

    @Column(name = "destination_label", length = 300)
    private String destinationLabel;

    @Column(name = "responsible_user_id")
    private Long responsibleUserId;

    @Column(name = "responsible_user_name", length = 300)
    private String responsibleUserName;

    @Column(name = "signed_by_user_id")
    private Long signedByUserId;

    @Column(name = "signed_by_user_name", length = 300)
    private String signedByUserName;

    @Column(name = "signature_url", length = 1000)
    private String signatureUrl;

    @Column(name = "support_number", length = 100)
    private String supportNumber;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
