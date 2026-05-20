package backend_instituciones.backend_instituciones.dto.response;

import backend_instituciones.backend_instituciones.domain.enums.Role;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserResponse {
    private Long id;
    private Long institutionId;
    private String name;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String documentType;
    private String documentNumber;
    private LocalDate birthDate;
    private String gender;
    private String address;
    private String district;
    private String city;
    private String photoUrl;
    private Role role;
    private String status;
    private boolean active;
    private boolean mustCompleteProfile;
    private boolean mustChangePassword;

    // Student
    private String studentCode;
    private LocalDate admissionDate;
    private String bloodType;
    private String allergies;
    private String medicalNotes;
    private String specialNeeds;
    private String emergencyPhone;

    // Teacher/Admin
    private String employeeCode;
    private String specialty;
    private LocalDate hireDate;
    private String professionalLicense;
    private String position;

    // Guardian
    private String occupation;
    private String workplace;
    private String alternativePhone;
    private String billingEmail;

    private Map<String, Map<String, Boolean>> adminPermissions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
