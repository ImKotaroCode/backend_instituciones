package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "student_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "student_code", length = 30)
    private String studentCode;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "blood_type", length = 5)
    private String bloodType;

    @Column(name = "allergies", length = 500)
    private String allergies;

    @Column(name = "medical_notes", length = 1000)
    private String medicalNotes;

    @Column(name = "special_needs", length = 500)
    private String specialNeeds;

    @Column(name = "emergency_phone", length = 20)
    private String emergencyPhone;
}
