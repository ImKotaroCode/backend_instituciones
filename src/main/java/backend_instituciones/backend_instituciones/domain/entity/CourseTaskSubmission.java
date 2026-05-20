package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_task_submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CourseTaskSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "student_name", length = 200)
    private String studentName;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_name", length = 120)
    private String groupName;

    @Column(columnDefinition = "text")
    private String comment;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "attempt_number", nullable = false, columnDefinition = "integer default 1")
    @Builder.Default
    private int attemptNumber = 1;

    /** PENDIENTE | ENVIADO | REVISADO | TARDIO */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ENVIADO";

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(columnDefinition = "text")
    private String feedback;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
