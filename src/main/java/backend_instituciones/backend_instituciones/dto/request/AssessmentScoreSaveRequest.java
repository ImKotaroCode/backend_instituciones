package backend_instituciones.backend_instituciones.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AssessmentScoreSaveRequest {

    private List<ScoreItem> scores;

    @Data
    public static class ScoreItem {
        private Long studentId;
        private String studentName;
        private BigDecimal value;
        private String feedback;
    }
}
