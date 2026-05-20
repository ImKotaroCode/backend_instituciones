package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentAttendanceRecordRequest {

    @NotNull
    private Long studentId;

    private Long periodId;
    private String periodName;

    private String studentName;

    @NotNull
    private Long courseId;

    private String courseName;

    @NotNull
    private Long sectionId;

    @NotNull
    private Long teacherId;

    private String teacherName;

    @NotBlank
    private String date;

    private String startTime;

    private String endTime;

    @NotBlank
    private String status;
}
