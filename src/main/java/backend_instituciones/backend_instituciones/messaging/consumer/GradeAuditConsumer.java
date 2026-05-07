package backend_instituciones.backend_instituciones.messaging.consumer;

import backend_instituciones.backend_instituciones.config.RabbitMQConfig;
import backend_instituciones.backend_instituciones.domain.entity.GradeAudit;
import backend_instituciones.backend_instituciones.repository.GradeAuditRepository;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GradeAuditConsumer {

    private final GradeAuditRepository gradeAuditRepository;

    @RabbitListener(
            queues = RabbitMQConfig.GRADE_AUDIT_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void process(Map<String, Object> payload, Message message, Channel channel) throws IOException {

        long tag = message.getMessageProperties().getDeliveryTag();

        try {

            GradeAudit audit = GradeAudit.builder()
                    .gradeId(toLong(payload.get("gradeId")))
                    .oldScore(toBigDecimal(payload.get("oldValue")))
                    .newScore(toBigDecimal(payload.get("newValue")))
                    .changedBy(toLong(payload.get("userId")))
                    .build();

            gradeAuditRepository.save(audit);
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("GradeAuditConsumer error: {}", e.getMessage(), e);
            channel.basicNack(tag, false, false);
        }
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        return Long.valueOf(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        return new BigDecimal(value.toString());
    }
}