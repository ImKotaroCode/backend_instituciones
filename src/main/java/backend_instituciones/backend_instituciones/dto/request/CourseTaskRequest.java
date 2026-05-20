package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourseTaskRequest {

    private Long periodId;
    private String periodName;

    @NotBlank
    private String title;

    private String description;

    /** TAREA | PROYECTO | PRACTICA_EXAMEN | TALLER */
    @NotBlank
    private String category;

    @NotNull
    private LocalDateTime visibleFrom;

    @NotNull
    private LocalDateTime dueAt;

    private BigDecimal maxScore = BigDecimal.valueOf(20);

    private int maxAttempts = 1;

    private boolean allowLateSubmission = false;

    private boolean groupTask = false;

    /** BORRADOR | PUBLICADO | CERRADO */
    private String status = "BORRADOR";

    /** Extensiones: pdf, doc, jpg, mp4, etc. */
    private List<String> acceptedFormats;

    /** Solo si groupTask = true */
    private List<TaskGroupRequest> groups;
}
