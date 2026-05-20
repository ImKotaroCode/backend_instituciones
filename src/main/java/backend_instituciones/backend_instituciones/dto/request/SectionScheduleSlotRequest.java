package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SectionScheduleSlotRequest {

    private String id; // frontend may send composite string like "2-MONDAY-08:00-08:45" or numeric id

    @NotNull
    private Long sectionId;

    @NotNull
    private Long gradeId;

    @NotNull
    private Long levelId;

    @NotNull
    private Long courseId;

    private String courseName;

    @NotNull
    private Long teacherId;

    @NotBlank
    private String weekday;

    @NotBlank
    private String startTime;

    @NotBlank
    private String endTime;

    private String classroomName;
}
