package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_task_allowed_formats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseTaskAllowedFormat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(nullable = false, length = 20)
    private String extension;

    @Column(name = "mime_type", length = 120)
    private String mimeType;
}
