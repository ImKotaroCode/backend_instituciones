package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_tasks",
        indexes = {
                @Index(name = "idx_ct_institution_course", columnList = "institution_id, course_id"),
                @Index(name = "idx_ct_course_period", columnList = "course_id, period_id")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CourseTask {

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

    /** TAREA | PROYECTO | PRACTICA_EXAMEN | TALLER */
    @Column(nullable = false, length = 40)
    private String category;

    @Column(name = "visible_from", nullable = false)
    private LocalDateTime visibleFrom;

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    @Column(name = "max_score", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal maxScore = BigDecimal.valueOf(20);

    @Column(name = "allow_late_submission", nullable = false)
    @Builder.Default
    private boolean allowLateSubmission = false;

    @Column(name = "is_group_task", nullable = false)
    @Builder.Default
    private boolean groupTask = false;

    @Column(name = "period_id")
    private Long periodId;

    @Column(name = "period_name", length = 60)
    private String periodName;

    @Column(name = "generated_code", length = 60, unique = true)
    private String generatedCode;

    @Column(name = "max_attempts", nullable = false, columnDefinition = "integer default 1")
    @Builder.Default
    private int maxAttempts = 1;

    /** BORRADOR | PUBLICADO | CERRADO */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "BORRADOR";

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
