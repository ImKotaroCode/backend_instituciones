package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequest {
    // Common fields
    @Size(max = 100) private String firstName;
    @Size(max = 100) private String lastName;
    @Size(max = 20)  private String phone;
    @Size(max = 20)  private String alternativePhone;
    @Size(max = 20)  private String documentType;
    @Size(max = 30)  private String documentNumber;
    private LocalDate birthDate;
    @Size(max = 10)  private String gender;
    @Size(max = 500) private String address;
    @Size(max = 100) private String district;
    @Size(max = 100) private String city;

    // Student
    @Size(max = 30)   private String studentCode;
    private LocalDate admissionDate;
    @Size(max = 5)    private String bloodType;
    @Size(max = 500)  private String allergies;
    @Size(max = 1000) private String medicalNotes;
    @Size(max = 500)  private String specialNeeds;
    @Size(max = 20)   private String emergencyPhone;

    // Teacher / Director / Admin
    @Size(max = 30)  private String employeeCode;
    @Size(max = 100) private String specialty;
    private LocalDate hireDate;
    @Size(max = 50)  private String professionalLicense;
    @Size(max = 100) private String position;

    // Guardian
    @Size(max = 100) private String occupation;
    @Size(max = 200) private String workplace;
    @Size(max = 200) private String billingEmail;
}
