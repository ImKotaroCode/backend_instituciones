package backend_instituciones.backend_instituciones.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionScheduleSlotResponse {
    private Long id;
    private Long sectionId;
    private Long gradeId;
    private Long levelId;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private String weekday;
    private String startTime;
    private String endTime;
    private String classroomName;
}
