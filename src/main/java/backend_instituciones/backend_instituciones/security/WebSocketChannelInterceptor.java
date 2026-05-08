package backend_instituciones.backend_instituciones.security;

import backend_instituciones.backend_instituciones.domain.entity.User;
import backend_instituciones.backend_instituciones.repository.UserRepository;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        log.info("STOMP CONNECT — native headers: {}", accessor.toNativeHeaderMap());

        String token = extractToken(accessor);
        log.info("STOMP CONNECT — token: {}", token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "NULL");

        if (token == null) {
            log.warn("STOMP CONNECT rejected — missing token");
            throw new MessageDeliveryException("UNAUTHORIZED: missing token");
        }

        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(token);
        } catch (JwtException e) {
            log.warn("STOMP CONNECT rejected — invalid token: {}", e.getMessage());
            throw new MessageDeliveryException("UNAUTHORIZED: invalid token");
        }

        String supabaseUid = jwt.getSubject();
        Optional<User> userOpt = userRepository.findBySupabaseUid(supabaseUid);

        if (userOpt.isEmpty()) {
            log.warn("STOMP CONNECT rejected — user not found for supabaseUid: {}", supabaseUid);
            throw new MessageDeliveryException("UNAUTHORIZED: user not found");
        }

        User user = userOpt.get();

        if (!"ADMIN".equals(user.getRole().name())) {
            log.warn("STOMP CONNECT rejected — role {} not allowed", user.getRole());
            throw new MessageDeliveryException("FORBIDDEN: ADMIN role required");
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                supabaseUid, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        accessor.setUser(auth);
        log.info("STOMP CONNECT — authenticated: {}, role: {}", supabaseUid, user.getRole());

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
