package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_purchase_quotations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehousePurchaseQuotation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "purchase_order_id", nullable = false)
    private Long purchaseOrderId;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "supplier_name", length = 300)
    private String supplierName;

    @Column(name = "quoted_at")
    private LocalDate quotedAt;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "pdf_url", length = 1000)
    private String pdfUrl;

    @Column(name = "pdf_name", length = 300)
    private String pdfName;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** REGISTRADA | SELECCIONADA | DESCARTADA */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "REGISTRADA";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
