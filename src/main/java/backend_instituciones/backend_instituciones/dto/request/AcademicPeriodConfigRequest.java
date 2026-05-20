package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AcademicPeriodConfigRequest {

    @NotBlank
    private String scheme;

    @NotNull
    private List<PeriodItem> periods;

    @Data
    public static class PeriodItem {
        private Long id;

        @NotBlank
        private String name;

        @NotBlank
        private String code;

        private String startDate;
        private String endDate;
        private int sortOrder;
        private boolean active = true;
        private boolean current = false;
    }
}
