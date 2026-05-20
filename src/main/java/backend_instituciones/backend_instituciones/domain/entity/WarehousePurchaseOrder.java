package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_purchase_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehousePurchaseOrder {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "order_number", length = 50)
    private String orderNumber;

    @Column(length = 500)
    private String title;

    @Column(name = "item_description", columnDefinition = "TEXT")
    private String itemDescription;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", length = 200)
    private String categoryName;

    @Column
    private Integer quantity;

    /** BAJA | MEDIA | ALTA */
    @Column(length = 10)
    private String urgency;

    @Column(name = "requested_by_user_id")
    private Long requestedByUserId;

    @Column(name = "requested_by_user_name", length = 300)
    private String requestedByUserName;

    @Column(name = "requested_at")
    private LocalDate requestedAt;

    /** BORRADOR | COTIZANDO | POR_APROBAR | APROBADO | RECIBIDO | CON_INCIDENCIA | CERRADO */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "BORRADOR";

    @Column(name = "approved_quotation_id")
    private Long approvedQuotationId;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "supplier_name", length = 300)
    private String supplierName;

    @Column(name = "approval_note", columnDefinition = "TEXT")
    private String approvalNote;

    @Column(name = "document_url", length = 1000)
    private String documentUrl;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
