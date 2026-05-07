package backend_instituciones.backend_instituciones.messaging;

import backend_instituciones.backend_instituciones.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendEmail(Map<String, Object> payload) {
        send(RabbitMQConfig.EXCHANGE, "notification.email.send", payload);
    }

    public void sendGradeAudit(Map<String, Object> payload) {
        send(RabbitMQConfig.EXCHANGE, "grade.audit.save", payload);
    }

    public void sendAnnouncement(Map<String, Object> payload) {
        send(RabbitMQConfig.EXCHANGE, "announcement.publish", payload);
    }

    public void sendReportExport(Map<String, Object> payload) {
        send(RabbitMQConfig.EXCHANGE, "report.export.start", payload);
    }

    private void send(String exchange, String routingKey, Object payload) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, payload);
            log.debug("Sent message to {}/{}", exchange, routingKey);
        } catch (Exception e) {
            log.error("Failed to send message to {}/{}: {}", exchange, routingKey, e.getMessage());
        }
    }
}
