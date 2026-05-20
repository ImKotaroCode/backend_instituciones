package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "student_attendance_records",
        indexes = {
                @Index(name = "idx_sar_institution_date", columnList = "institution_id, attendance_date"),
                @Index(name = "idx_sar_student", columnList = "institution_id, student_id"),
                @Index(name = "idx_sar_course", columnList = "institution_id, course_id"),
                @Index(name = "idx_sar_section", columnList = "institution_id, section_id"),
                @Index(name = "idx_sar_teacher", columnList = "institution_id, teacher_id"),
                @Index(name = "idx_sar_lookup", columnList = "institution_id, student_id, course_id, attendance_date"),
                @Index(name = "idx_sar_session", columnList = "session_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentAttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "student_name", length = 200)
    private String studentName;

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

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "period_id")
    private Long periodId;

    @Column(name = "period_name", length = 100)
    private String periodName;

    @Column(nullable = false, length = 20)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
