package backend_instituciones.backend_instituciones.messaging.consumer;

import backend_instituciones.backend_instituciones.config.RabbitMQConfig;
import backend_instituciones.backend_instituciones.domain.entity.ExportJob;
import backend_instituciones.backend_instituciones.domain.enums.ExportJobStatus;
import backend_instituciones.backend_instituciones.repository.ExportJobRepository;
import backend_instituciones.backend_instituciones.service.SseService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportExportConsumer {

    private final ExportJobRepository exportJobRepository;
    private final SseService sseService;

    @RabbitListener(queues = RabbitMQConfig.REPORT_EXPORT_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void process(Map<String, Object> payload, Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        Long jobId = Long.valueOf(payload.get("jobId").toString());
        String userId = (String) payload.get("userId");

        try {
            ExportJob job = exportJobRepository.findById(jobId).orElseThrow();

            // 1. Mark PROCESSING
            job.setStatus(ExportJobStatus.PROCESSING);
            exportJobRepository.save(job);
            sseService.sendToUser(userId, "export-progress", Map.of("jobId", jobId, "progress", 10));

            // 2. Simulate PDF generation (replace with real Jasper/iText logic)
            Thread.sleep(1000);
            sseService.sendToUser(userId, "export-progress", Map.of("jobId", jobId, "progress", 80));

            // 3. Mock upload URL (replace with Supabase Storage upload)
            String fileUrl = "https://ypfshplscauigbhixkky.storage.supabase.co/storage/v1/s3" + jobId + ".pdf";

            job.setStatus(ExportJobStatus.DONE);
            job.setFileUrl(fileUrl);
            job.setCompletedAt(LocalDateTime.now());
            exportJobRepository.save(job);

            sseService.sendToUser(userId, "export-progress", Map.of(
                    "jobId", jobId,
                    "status", "DONE",
                    "downloadUrl", fileUrl,
                    "expiresIn", "24h"
            ));

            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("ReportExportConsumer error: {}", e.getMessage());
            exportJobRepository.findById(jobId).ifPresent(job -> {
                job.setStatus(ExportJobStatus.FAILED);
                exportJobRepository.save(job);
            });
            sseService.sendToUser(userId, "export-progress", Map.of("jobId", jobId, "status", "FAILED"));
            channel.basicNack(tag, false, false);
        }
    }
}
