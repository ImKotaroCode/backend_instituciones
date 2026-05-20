package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_assets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseAsset {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    /** Auto-generated: CATEGORY_PREFIX-XXXXXX */
    @Column(unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 300)
    private String name;

    @Column(name = "category_id")
    private Long categoryId;

    /** Category name snapshot (desnormalizado para historial) */
    @Column(length = 100)
    private String category;

    /** MOBILIARIO | TECNOLOGIA | DIDACTICO | INFRAESTRUCTURA */
    @Column(name = "main_category", length = 50)
    private String mainCategory;

    @Column(length = 100)
    private String brand;

    @Column(length = 100)
    private String model;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(length = 50)
    private String color;

    /** NUEVO | BUENO | REGULAR | BAJA */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "BUENO";

    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;

    @Column(name = "initial_value", precision = 12, scale = 2)
    private BigDecimal initialValue;

    @Column(name = "photo_url", length = 1000)
    private String photoUrl;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_name", length = 200)
    private String roomName;

    @Column(name = "pavilion_name", length = 200)
    private String pavilionName;

    @Column(name = "sector_name", length = 200)
    private String sectorName;

    @Column(name = "teacher_responsible_id")
    private Long teacherResponsibleId;

    @Column(name = "teacher_responsible_name", length = 300)
    private String teacherResponsibleName;

    @Column(name = "warranty_until")
    private LocalDate warrantyUntil;

    // ── Reporte de condición ──────────────────────────────────────────────────

    @Column(name = "reported_by_name", length = 300)
    private String reportedByName;

    @Column(name = "reported_at")
    private LocalDate reportedAt;

    @Column(name = "report_observation", columnDefinition = "TEXT")
    private String reportObservation;

    /** NINGUNA | ENVIAR_MANTENIMIENTO | REUBICAR | DAR_BAJA */
    @Column(name = "pending_action", length = 30)
    @Builder.Default
    private String pendingAction = "NINGUNA";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
