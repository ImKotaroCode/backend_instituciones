package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_assessments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CourseAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, length = 100)
    private String period;

    @Column(name = "period_id")
    private Long periodId;

    @Column(name = "period_name", length = 60)
    private String periodName;

    /** EXAMEN | QUIZ | PROYECTO | TAREA | PARTICIPACION */
    @Column(nullable = false, length = 40)
    private String type;

    @Column(name = "max_score", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal maxScore = BigDecimal.valueOf(20);

    @Column(name = "passing_score", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal passingScore = BigDecimal.valueOf(11);

    /** Peso porcentual del componente. Suma de todos no debe superar 100 */
    @Column(name = "weight_percentage", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal weightPercentage = BigDecimal.ZERO;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
