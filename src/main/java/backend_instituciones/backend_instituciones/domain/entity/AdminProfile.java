package backend_instituciones.backend_instituciones.domain.entity;

import backend_instituciones.backend_instituciones.domain.converter.JsonMapConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "admin_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "employee_code", length = 30)
    private String employeeCode;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "position", length = 100)
    private String position;

    @Convert(converter = JsonMapConverter.class)
    @Column(name = "admin_permissions", columnDefinition = "text")
    private Map<String, Map<String, Boolean>> adminPermissions;
}
