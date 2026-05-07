package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AcademicYearResponse {
    private Long id;
    private Long institutionId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private boolean isCurrent;
    private LocalDateTime createdAt;
}
