package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "classrooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "grade_level_id")
    private Long gradeLevelId;

    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "academic_year", nullable = false, length = 10)
    private String academicYear;

    @Column(name = "academic_level_id")
    private Long academicLevelId;

    @Column(name = "academic_grade_id")
    private Long academicGradeId;

    @Column(name = "academic_section_id")
    private Long academicSectionId;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "tutor_teacher_id")
    private Long tutorTeacherId;

    @Column(name = "academic_year_id")
    private Long academicYearId;

    @Column
    private Integer capacity;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
