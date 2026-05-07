package backend_instituciones.backend_instituciones.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        log.info("STOMP CONNECT — native headers: {}", accessor.toNativeHeaderMap());

        String token = extractToken(accessor);
        log.info("STOMP CONNECT — extracted token: {}", token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "NULL");

        if (token == null || !jwtService.isValid(token)) {
            log.warn("STOMP CONNECT rejected — invalid or missing token");
            throw new MessageDeliveryException("UNAUTHORIZED: missing or invalid token");
        }

        String userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        if (!"ADMIN".equals(role)) {
            log.warn("STOMP CONNECT rejected — role {} is not allowed", role);
            throw new MessageDeliveryException("FORBIDDEN: ADMIN role required");
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        accessor.setUser(auth);
        log.info("STOMP CONNECT — authenticated user: {}, role: {}", userId, role);

        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        for (String headerName : new String[]{"Authorization", "authorization", "AUTHORIZATION"}) {
            String val = accessor.getFirstNativeHeader(headerName);
            if (val != null) {
                if (val.startsWith("Bearer ")) return val.substring(7);
                if (val.startsWith("bearer ")) return val.substring(7);
                return val;
            }
        }

        String tokenHeader = accessor.getFirstNativeHeader("token");
        if (tokenHeader != null) return tokenHeader;

        Map<String, Object> attrs = accessor.getSessionAttributes();
        if (attrs != null) {
            Object sessionToken = attrs.get("token");
            if (sessionToken instanceof String s) return s;
        }

        return null;
    }
}
