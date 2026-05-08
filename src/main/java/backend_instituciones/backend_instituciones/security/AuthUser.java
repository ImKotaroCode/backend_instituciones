package backend_instituciones.backend_instituciones.security;

import backend_instituciones.backend_instituciones.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthUser {
    private final String supabaseUid;
    private final Long institutionId;
    private final Long userId;
    private final Role role;
}
