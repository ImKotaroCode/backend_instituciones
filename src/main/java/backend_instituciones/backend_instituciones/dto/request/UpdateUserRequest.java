package backend_instituciones.backend_instituciones.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class UpdateUserRequest {
    private String name;
    private String password;
    private Map<String, Map<String, Boolean>> adminPermissions;
}
