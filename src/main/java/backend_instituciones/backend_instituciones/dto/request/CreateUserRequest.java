package backend_instituciones.backend_instituciones.dto.request;

import backend_instituciones.backend_instituciones.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class CreateUserRequest {
    @NotBlank
    private String name;
    @NotBlank @Email
    private String email;
    @NotNull
    private Role role;
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$",
             message = "LA CONTRASEÑA DEBE TENER +8 CARACTERES, CON UNA MAYUSCULA, UN NUMERO Y UN SIMBOLO")
    private String password;

    // Datos personales
    private String phone;
    private String alternativePhone;
    private String documentType;
    private String documentNumber;
    private LocalDate birthDate;
    private String gender;
    private String address;
    private String district;
    private String city;
    private String emergencyPhone;
    private String bloodType;
    private String allergies;
    private String medicalNotes;
    private String specialNeeds;

    // Datos laborales/académicos (DOCENTE / ADMIN)
    private LocalDate hireDate;
    private String position;
    private String specialty;
    private String professionalLicense;

    // Datos PADRE/tutor
    private String occupation;
    private String workplace;
    private String billingEmail;

    // Datos ESTUDIANTE
    private LocalDate admissionDate;
    // Asignacion directa al crear estudiante
    private Long levelId;
    private Long gradeId;
    private Long sectionId;

    // Datos PADRE — IDs de estudiantes vinculados
    private List<Long> linkedStudentIds;

    // Permisos finos para rol ADMINISTRACION
    private Map<String, Map<String, Boolean>> adminPermissions;
}
