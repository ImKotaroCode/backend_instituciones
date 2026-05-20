package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_student_profiles",
        uniqueConstraints = @UniqueConstraint(columnNames = {"institution_id", "student_id", "academic_year_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentStudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "student_name", length = 200)
    private String studentName;

    @Column(name = "student_dni", length = 30)
    private String studentDni;

    @Column(name = "academic_year_id", nullable = false)
    private Long academicYearId;

    @Column(name = "academic_year_name", length = 100)
    private String academicYearName;

    @Column(name = "enrollment_type_id", nullable = false)
    private Long enrollmentTypeId;

    @Column(name = "monthly_scale_id", nullable = false)
    private Long monthlyScaleId;

    @Column(name = "level_id")
    private Long levelId;

    @Column(name = "grade_id")
    private Long gradeId;

    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "level_name", length = 150)
    private String levelName;

    @Column(name = "grade_name", length = 150)
    private String gradeName;

    @Column(name = "section_name", length = 150)
    private String sectionName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
