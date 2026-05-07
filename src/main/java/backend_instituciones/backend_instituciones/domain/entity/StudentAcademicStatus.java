package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_academic_statuses", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "academic_year_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentAcademicStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "academic_year_id", nullable = false)
    private Long academicYearId;

    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(length = 500)
    private String observation;

    @Column(name = "set_by_user_id")
    private Long setByUserId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
