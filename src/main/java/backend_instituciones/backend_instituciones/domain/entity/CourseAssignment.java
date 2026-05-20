package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_assignments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"classroom_id", "course_id"}),
                @UniqueConstraint(columnNames = {"generated_code"})
        },
        indexes = {
                @Index(name = "idx_ca_institution", columnList = "institution_id"),
                @Index(name = "idx_ca_classroom", columnList = "institution_id, classroom_id"),
                @Index(name = "idx_ca_teacher", columnList = "institution_id, teacher_user_id")
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

    @Column(name = "course_name", length = 200)
    private String courseName;

    /** Denormalized — set at creation, avoids 4 extra queries per response */
    @Column(name = "classroom_name", length = 300)
    private String classroomName;

    @Column(name = "academic_year", length = 50)
    private String academicYear;

    @Column(name = "level_name", length = 150)
    private String levelName;

    @Column(name = "grade_name", length = 150)
    private String gradeName;

    @Column(name = "section_name", length = 150)
    private String sectionName;

    @Column(name = "level_id")
    private Long levelId;

    @Column(name = "grade_id")
    private Long gradeId;

    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "teacher_user_id", nullable = false)
    private Long teacherId;

    /** Denormalized teacher name — updated when teacher reassigned */
    @Column(name = "teacher_name", length = 200)
    private String teacherName;

    @Column(name = "generated_code", nullable = false, length = 50)
    private String generatedCode;

    @Column(length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
