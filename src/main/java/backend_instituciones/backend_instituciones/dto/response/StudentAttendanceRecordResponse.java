package backend_instituciones.backend_instituciones.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttendanceRecordResponse {
    private Long id;
    private Long periodId;
    private String periodName;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private Long sectionId;
    private Long teacherId;
    private String teacherName;
    private String date;
    private String startTime;
    private String endTime;
    private String status;
}
