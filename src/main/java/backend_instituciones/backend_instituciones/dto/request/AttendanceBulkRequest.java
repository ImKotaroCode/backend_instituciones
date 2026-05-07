package backend_instituciones.backend_instituciones.dto.request;

import backend_instituciones.backend_instituciones.domain.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class AttendanceBulkRequest {
    @NotNull
    private Long courseId;
    @NotNull
    private LocalDate date;
    @NotNull
    private List<AttendanceRecord> records;

    @Data
    public static class AttendanceRecord {
        @NotNull
        private Long studentId;
        @NotNull
        private AttendanceStatus status;
    }
}
