package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class EnrollRequest {
    @NotNull
    private List<Long> studentIds;
}
