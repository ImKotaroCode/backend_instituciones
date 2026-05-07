package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.ExportJob;
import backend_instituciones.backend_instituciones.domain.enums.ExportJobStatus;
import backend_instituciones.backend_instituciones.dto.response.ExportJobResponse;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.messaging.RabbitMQProducer;
import backend_instituciones.backend_instituciones.repository.ExportJobRepository;
import backend_instituciones.backend_instituciones.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ExportJobRepository exportJobRepository;
    private final GradeRepository gradeRepository;
    private final RabbitMQProducer producer;

    @Transactional
    public ExportJobResponse requestExport(Long institutionId, Long requestedBy, String reportType) {
        ExportJob job = ExportJob.builder()
                .institutionId(institutionId)
                .requestedBy(requestedBy)
                .reportType(reportType)
                .status(ExportJobStatus.PENDING)
                .build();

        ExportJob saved = exportJobRepository.save(job);

        producer.sendReportExport(Map.of(
                "jobId", saved.getId().toString(),
                "reportType", reportType,
                "institutionId", institutionId.toString(),
                "userId", requestedBy.toString()
        ));

        return toResponse(saved);
    }

    public ExportJobResponse getJobStatus(Long jobId, Long institutionId) {
        return toResponse(exportJobRepository.findByIdAndInstitutionId(jobId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("ExportJob", jobId)));
    }

    public Object getAcademicSummary(Long institutionId) {
        return Map.of(
                "institutionId", institutionId,
                "totalGrades", gradeRepository.findByCourseIdAndInstitutionId(null, institutionId).size()
        );
    }

    private ExportJobResponse toResponse(ExportJob job) {
        return ExportJobResponse.builder()
                .jobId(job.getId())
                .status(job.getStatus())
                .fileUrl(job.getFileUrl())
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .build();
    }
}
