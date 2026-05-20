package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_inventories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseInventory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_name", length = 200)
    private String roomName;

    @Column(name = "counted_at")
    private LocalDateTime countedAt;

    @Column(name = "expected_count")
    private Integer expectedCount;

    @Column(name = "physical_count")
    private Integer physicalCount;

    private Integer difference;

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
