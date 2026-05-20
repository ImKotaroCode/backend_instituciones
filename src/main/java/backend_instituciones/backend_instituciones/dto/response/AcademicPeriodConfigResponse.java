package backend_instituciones.backend_instituciones.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AcademicPeriodConfigResponse {

    private String scheme;
    private List<PeriodDto> periods;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PeriodDto {
        private Long id;
        private String name;
        private String code;
        private String scheme;
        private String startDate;
        private String endDate;
        private int sortOrder;
        private boolean active;
        private boolean current;
    }
}
