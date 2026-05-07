package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseService {

    private final JwtService jwtService;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(300_000L);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of("userId", userId, "ts", Instant.now().toString())));
        } catch (IOException e) {
            emitters.remove(userId);
        }

        return emitter;
    }

    public void sendToUser(String userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }

    public void broadcast(String institutionId, String eventName, Object data) {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        });
    }

    public void sendHeartbeat() {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .data(Map.of("ts", Instant.now().toString())));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        });
    }
    public String extractUserId(String token) {
        return jwtService.extractUserId(token);
    }

    public boolean isValid(String token) {
        return jwtService.isValid(token);
    }
}
