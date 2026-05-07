package backend_instituciones.backend_instituciones.messaging.consumer;

import backend_instituciones.backend_instituciones.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class EmailConsumer {

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void process(Map<String, Object> payload, Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("Sending email to={} subject={}", payload.get("to"), payload.get("subject"));
            // TODO: integrate with JavaMailSender or third-party email service
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("EmailConsumer error: {}", e.getMessage());
            long retryCount = getRetryCount(message);
            if (retryCount >= 3) {
                channel.basicReject(tag, false);
            } else {
                channel.basicNack(tag, false, true);
            }
        }
    }

    private long getRetryCount(Message message) {
        Object count = message.getMessageProperties().getHeaders().get("x-death");
        if (count instanceof java.util.List<?> list && !list.isEmpty()) {
            Object first = list.getFirst();
            if (first instanceof Map<?, ?> map) {
                Object cnt = map.get("count");
                if (cnt instanceof Number n) return n.longValue();
            }
        }
        return 0;
    }
}
