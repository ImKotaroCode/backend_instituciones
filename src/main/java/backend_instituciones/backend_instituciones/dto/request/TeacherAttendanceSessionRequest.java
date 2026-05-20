package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class TeacherAttendanceSessionRequest {

    @NotNull
    private Long teacherId;

    private String teacherName;

    @NotNull
    private Long courseId;

    private String courseName;

    @NotNull
    private Long sectionId;

    private Long levelId;

    private Long gradeId;

    @NotBlank
    private String date;

    @NotBlank
    private String startTime;

    @NotBlank
    private String endTime;

    private String checkInTime;

    @NotBlank
    private String teacherStatus;

    private Integer tardinessMinutes;

    private Map<String, String> studentStatusMap;
}
