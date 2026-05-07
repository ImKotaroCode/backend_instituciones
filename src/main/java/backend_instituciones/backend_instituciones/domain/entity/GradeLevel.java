package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "grade_levels", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"institution_id", "name"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GradeLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(length = 50)
    private String level;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
