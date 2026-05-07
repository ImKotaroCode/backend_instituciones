package backend_instituciones.backend_instituciones.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketSecurityConfig {
    // Sin seguridad de mensajería WebSocket.
    // Seguridad ya se maneja en SecurityConfig y en el handshake/interceptor.
}
