package backend_instituciones.backend_instituciones.messaging.consumer;

import backend_instituciones.backend_instituciones.config.RabbitMQConfig;
import backend_instituciones.backend_instituciones.service.SseService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnnouncementConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final SseService sseService;

    @RabbitListener(queues = RabbitMQConfig.ANNOUNCEMENT_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void process(Map<String, Object> payload, Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            String institutionId = (String) payload.get("institutionId");
            String title = (String) payload.get("title");

            // 1. WebSocket broadcast to institution topic
            messagingTemplate.convertAndSend(
                    "/topic/institution/" + institutionId + "/announcements",
                    (Object) payload
            );

            // 2. SSE broadcast
            sseService.broadcast(institutionId, "announcement", payload);

            log.info("Announcement published: {}", title);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("AnnouncementConsumer error: {}", e.getMessage());
            channel.basicNack(tag, false, false);
        }
    }
}
