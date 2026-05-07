package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_assignments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"classroom_id", "course_id"}),
        @UniqueConstraint(columnNames = {"generated_code"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "teacher_user_id", nullable = false)
    private Long teacherId;

    @Column(name = "generated_code", nullable = false, length = 50)
    private String generatedCode;

    @Column(length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
