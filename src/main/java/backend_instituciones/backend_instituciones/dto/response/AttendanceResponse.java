package backend_instituciones.backend_instituciones.dto.response;

import backend_instituciones.backend_instituciones.domain.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data @Builder
public class AttendanceResponse {
    private Long id;
    private Long courseId;
    private Long studentId;
    private LocalDate date;
    private AttendanceStatus status;
    private Long registeredBy;
}
