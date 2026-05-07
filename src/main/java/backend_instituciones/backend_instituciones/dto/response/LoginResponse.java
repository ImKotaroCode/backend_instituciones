package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private UserInfo user;

    @Data @Builder
    public static class UserInfo {
        private Long id;
        private String name;
        private String role;
        private Long institutionId;
    }
}
