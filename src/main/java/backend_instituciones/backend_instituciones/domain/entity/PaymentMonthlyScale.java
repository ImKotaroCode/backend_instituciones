package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_monthly_scales")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentMonthlyScale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "monthly_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyAmount;

    /** NONE | DAILY */
    @Column(name = "late_mode", nullable = false, length = 10)
    @Builder.Default
    private String lateMode = "NONE";

    /** Amount per day if lateMode = DAILY */
    @Column(name = "late_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal lateAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
