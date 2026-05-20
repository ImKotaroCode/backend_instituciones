package backend_instituciones.backend_instituciones.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StudentSectionResponse {
    private Long userId;
    private Long levelId;
    private Long gradeId;
    private Long sectionId;
    private String levelName;
    private String gradeName;
    private String sectionName;
}
