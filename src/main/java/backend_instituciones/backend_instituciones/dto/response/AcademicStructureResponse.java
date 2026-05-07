package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AcademicStructureResponse {
    private List<LevelResponse> levels;

    @Data
    @Builder
    public static class LevelResponse {
        private Long id;
        private String name;
        private String code;
        private Integer sortOrder;
        private String status;
        private List<GradeResponse> grades;
    }

    @Data
    @Builder
    public static class GradeResponse {
        private Long id;
        private String name;
        private Integer sortOrder;
        private String status;
        private List<SectionResponse> sections;
    }

    @Data
    @Builder
    public static class SectionResponse {
        private Long id;
        private String name;
        private Integer sortOrder;
        private String status;
    }
}
