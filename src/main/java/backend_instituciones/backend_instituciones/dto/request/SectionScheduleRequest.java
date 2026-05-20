package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SectionScheduleRequest {

    @NotNull
    private List<SectionScheduleSlotRequest> slots;
}
