package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseRoom {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "sector_id")
    private Long sectorId;

    @Column(name = "pavilion_id")
    private Long pavilionId;

    @Column(name = "sector_name", length = 200)
    private String sectorName;

    @Column(name = "pavilion_name", length = 200)
    private String pavilionName;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 50)
    private String code;

    @Column(name = "room_type", length = 100)
    private String roomType;

    @Column(length = 20)
    private String floor;

    private Integer capacity;

    private Integer aforo;

    @Column(name = "teacher_responsible_id")
    private Long teacherResponsibleId;

    @Column(name = "teacher_responsible_name", length = 300)
    private String teacherResponsibleName;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
