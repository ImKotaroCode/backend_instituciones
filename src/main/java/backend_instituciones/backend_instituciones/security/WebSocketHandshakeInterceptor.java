package backend_instituciones.backend_instituciones.security;

import backend_instituciones.backend_instituciones.domain.entity.User;
import backend_instituciones.backend_instituciones.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            String rawToken = extractToken(servletRequest);
            String token = rawToken != null ? rawToken.trim() : null;

            if (token != null) {
                try {
                    Jwt jwt = jwtDecoder.decode(token);
                    String supabaseUid = jwt.getSubject();

                    Optional<User> userOpt = userRepository.findBySupabaseUid(supabaseUid);
                    userOpt.ifPresent(user -> {
                        attributes.put("userId", user.getId().toString());
                        attributes.put("institutionId", user.getInstitutionId().toString());
                        attributes.put("role", user.getRole().name());
                        attributes.put("supabaseUid", supabaseUid);
                        attributes.put("token", token);
                    });
                } catch (JwtException e) {
                    // Invalid token — STOMP CONNECT enforces auth
                }
            }
        }

        // Always allow handshake; STOMP CONNECT handles auth enforcement
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private String extractToken(ServletServerHttpRequest request) {
        String auth = request.getServletRequest().getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) return auth.substring(7);
        return request.getServletRequest().getParameter("token");
    }
}
