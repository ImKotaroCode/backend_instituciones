package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_voucher_submissions",
        uniqueConstraints = @UniqueConstraint(name = "uq_voucher_charge", columnNames = "charge_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentVoucherSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "charge_id", nullable = false)
    private Long chargeId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "parent_id", nullable = false)
    private Long parentId;

    @Column(name = "academic_year_id")
    private Long academicYearId;

    @Column(name = "student_name", length = 300)
    private String studentName;

    @Column(name = "student_photo_url", length = 1000)
    private String studentPhotoUrl;

    @Column(name = "student_dni", length = 20)
    private String studentDni;

    /** Public URL of voucher image (stored as WEBP) */
    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Column(name = "image_name", length = 300)
    private String imageName;

    /** SUBMITTED | APPROVED | REJECTED */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "SUBMITTED";

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewer_name", length = 300)
    private String reviewerName;

    @Column(name = "review_note", length = 1000)
    private String reviewNote;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
