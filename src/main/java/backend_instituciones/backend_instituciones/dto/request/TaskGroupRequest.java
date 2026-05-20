package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class TaskGroupRequest {

    @NotBlank
    private String name;

    private List<Long> studentIds;
}
