package backend_instituciones.backend_instituciones.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AcademicStructureRequest {
    private List<LevelRequest> levels;

    @Data
    public static class LevelRequest {
        private Long id;
        private String name;
        private Integer sortOrder;
        private String status;
        private List<GradeRequest> grades;
    }

    @Data
    public static class GradeRequest {
        private Long id;
        private String name;
        private Integer sortOrder;
        private String status;
        private List<SectionRequest> sections;
    }

    @Data
    public static class SectionRequest {
        private Long id;
        private String name;
        private Integer sortOrder;
        private String status;
    }
}
