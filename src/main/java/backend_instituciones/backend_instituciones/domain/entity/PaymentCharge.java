package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_charges")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "academic_year_id", nullable = false)
    private Long academicYearId;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    /** ENROLLMENT | MONTHLY */
    @Column(nullable = false, length = 20)
    private String kind;

    /** Human-readable label: "Matricula regular", "Mensualidad Mayo 2026" */
    @Column(nullable = false, length = 200)
    private String label;

    /** Nullable — only for MONTHLY charges. Format: "2026-05" */
    @Column(name = "month_key", length = 7)
    private String monthKey;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "base_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseAmount;

    /** PENDING | PAID — OVERDUE is computed at response time */
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "paid_at")
    private LocalDate paidAt;

    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount;

    /** Nullable for MONTHLY */
    @Column(name = "enrollment_type_id")
    private Long enrollmentTypeId;

    /** Nullable for ENROLLMENT */
    @Column(name = "monthly_scale_id")
    private Long monthlyScaleId;

    @Column(name = "level_id")
    private Long levelId;

    @Column(name = "grade_id")
    private Long gradeId;

    @Column(name = "section_id")
    private Long sectionId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
