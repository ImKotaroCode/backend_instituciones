package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EnrollmentRequest {
    @NotNull
    private Long studentId;

    @NotNull
    private Long classroomId;

    @NotNull
    private Long academicYearId;

    private LocalDate enrollmentDate;

    private String status;
}
