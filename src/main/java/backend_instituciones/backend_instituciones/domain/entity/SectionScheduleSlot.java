package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "section_schedule_slots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"institution_id", "section_id", "weekday", "start_time", "end_time"}),
        indexes = {
                @Index(name = "idx_sss_institution", columnList = "institution_id"),
                @Index(name = "idx_sss_section", columnList = "institution_id, section_id"),
                @Index(name = "idx_sss_teacher", columnList = "institution_id, teacher_id"),
                @Index(name = "idx_sss_course", columnList = "institution_id, course_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SectionScheduleSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "level_id", nullable = false)
    private Long levelId;

    @Column(name = "grade_id", nullable = false)
    private Long gradeId;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "course_name", length = 200)
    private String courseName;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(nullable = false, length = 20)
    private String weekday;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "classroom_name", length = 180)
    private String classroomName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
