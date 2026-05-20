package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "teacher_attendance_sessions",
        uniqueConstraints = @UniqueConstraint(columnNames = {
                "institution_id", "course_id", "section_id", "teacher_id",
                "attendance_date", "start_time", "end_time"
        }),
        indexes = {
                @Index(name = "idx_tas_institution_date", columnList = "institution_id, attendance_date"),
                @Index(name = "idx_tas_teacher_date", columnList = "institution_id, teacher_id, attendance_date"),
                @Index(name = "idx_tas_course", columnList = "institution_id, course_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeacherAttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "teacher_name", length = 200)
    private String teacherName;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "course_name", length = 200)
    private String courseName;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Column(name = "level_id")
    private Long levelId;

    @Column(name = "grade_id")
    private Long gradeId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "teacher_status", nullable = false, length = 20)
    private String teacherStatus;

    @Column(name = "tardiness_minutes", nullable = false)
    @Builder.Default
    private int tardinessMinutes = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
