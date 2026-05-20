package backend_instituciones.backend_instituciones.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoCheckInResponse {
    private boolean matched;
    private Long courseId;
    private Long sectionId;
    private Long sessionId;
    private String teacherStatus;
    private String startTime;
    private String endTime;
}
