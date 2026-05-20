package backend_instituciones.backend_instituciones.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherMySummaryResponse {
    private int presentCount;
    private int lateCount;
    private int absentCount;
    private int totalTardinessMinutes;
}
