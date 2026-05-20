package backend_instituciones.backend_instituciones.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceAlertResponse {
    private String id;
    private Long teacherId;
    private String teacherName;
    private Long courseId;
    private String courseName;
    private Long sectionId;
    private String sectionName;
    private String startTime;
    private int minutesLate;
    private String type;
}
