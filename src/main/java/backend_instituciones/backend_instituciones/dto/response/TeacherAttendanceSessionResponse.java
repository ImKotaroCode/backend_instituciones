package backend_instituciones.backend_instituciones.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAttendanceSessionResponse {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private Long courseId;
    private String courseName;
    private Long sectionId;
    private Long levelId;
    private Long gradeId;
    private String date;
    private String startTime;
    private String endTime;
    private String checkInTime;
    private String teacherStatus;
    private int tardinessMinutes;
    private Map<String, String> studentStatusMap;
}
