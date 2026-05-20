package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_enrollments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "academic_year_id"})
        },
        indexes = {
                @Index(name = "idx_se_institution", columnList = "institution_id"),
                @Index(name = "idx_se_institution_year", columnList = "institution_id, academic_year_id"),
                @Index(name = "idx_se_classroom", columnList = "classroom_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;

    @Column(name = "academic_year_id", nullable = false)
    private Long academicYearId;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "promoted_from_enrollment_id")
    private Long promotedFromEnrollmentId;

    @Column(name = "repeating", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean repeating = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
