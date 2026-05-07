package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "teacher_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeacherProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "employee_code", length = 30)
    private String employeeCode;

    @Column(name = "specialty", length = 100)
    private String specialty;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "professional_license", length = 50)
    private String professionalLicense;
}
