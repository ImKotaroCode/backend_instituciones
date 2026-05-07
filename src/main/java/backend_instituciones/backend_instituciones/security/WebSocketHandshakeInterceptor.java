package backend_instituciones.backend_instituciones.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {

            String token = extractToken(servletRequest);

            if (token != null) {
                token = token.trim();
            }

            System.out.println("WS TOKEN: " + token);

            if (token != null && jwtService.isValid(token)) {

                System.out.println("TOKEN VALID ✅");

                attributes.put("userId", jwtService.extractUserId(token));
                attributes.put("institutionId", jwtService.extractInstitutionId(token));
                attributes.put("role", jwtService.extractRole(token));
                attributes.put("token", token);

            } else {
                System.out.println("TOKEN INVALID or missing — STOMP CONNECT will handle auth ⚠️");
            }
        }

        // ⚠️ IMPORTANTE: permitir handshake para evitar loop infinito
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private String extractToken(ServletServerHttpRequest request) {
        String auth = request.getServletRequest().getHeader("Authorization");

        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }

        return request.getServletRequest().getParameter("token");
    }
}