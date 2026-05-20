package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_assessment_scores",
       uniqueConstraints = @UniqueConstraint(columnNames = {"assessment_id", "student_id"}),
       indexes = {
               @Index(name = "idx_cas_assessment", columnList = "assessment_id"),
               @Index(name = "idx_cas_student", columnList = "student_id")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseAssessmentScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assessment_id", nullable = false)
    private Long assessmentId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "student_name", length = 200)
    private String studentName;

    @Column(precision = 5, scale = 2)
    private BigDecimal value;

    /** APROBADO | DESAPROBADO | PENDIENTE */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDIENTE";

    @Column(columnDefinition = "text")
    private String feedback;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", nullable = false)
    private Long updatedBy;
}
