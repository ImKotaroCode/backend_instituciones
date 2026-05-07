package backend_instituciones.backend_instituciones.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/class/{courseId}/start")
    public void classStarted(@DestinationVariable Long courseId,
                             @Payload Map<String, Object> payload) {
        messagingTemplate.convertAndSend(
                "/topic/class/" + courseId + "/status",
                (Object) Map.of("event", "CLASS_STARTED", "courseId", courseId, "payload", payload)
        );
    }

    @MessageMapping("/maintenance")
    public void maintenanceToggle(@Payload Map<String, Object> payload) {
        String institutionId = (String) payload.get("institutionId");
        messagingTemplate.convertAndSend(
                "/topic/institution/" + institutionId + "/maintenance",
                (Object) payload
        );
    }
}
