package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_task_group_members",
       uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "student_id"}),
       indexes = @Index(name = "idx_ctgm_student", columnList = "student_id"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseTaskGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;
}
