package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.Grade;
import backend_instituciones.backend_instituciones.dto.request.GradeRequest;
import backend_instituciones.backend_instituciones.dto.response.GradeResponse;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.messaging.RabbitMQProducer;
import backend_instituciones.backend_instituciones.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final RabbitMQProducer producer;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public List<GradeResponse> getByStudent(Long studentId, Long institutionId) {
        return gradeRepository.findByStudentIdAndInstitutionId(studentId, institutionId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getByCourse(Long courseId, Long institutionId) {
        return gradeRepository.findByCourseIdAndInstitutionId(courseId, institutionId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public GradeResponse create(Long institutionId, Long createdBy, GradeRequest request) {
        Grade grade = Grade.builder()
                .institutionId(institutionId)
                .courseId(request.getCourseId())
                .studentId(request.getStudentId())
                .period(request.getPeriod())
                .score(request.getScore())
                .observations(request.getObservations())
                .createdBy(createdBy)
                .build();

        Grade saved = gradeRepository.save(grade);

        producer.sendGradeAudit(Map.of(
                "gradeId", saved.getId().toString(),
                "oldValue", "null",
                "newValue", saved.getScore().toString(),
                "userId", createdBy.toString()
        ));

        messagingTemplate.convertAndSend(
                "/topic/institution/" + institutionId + "/grades-updated",
                (Object) Map.of("gradeId", saved.getId(), "courseId", saved.getCourseId())
        );

        return toResponse(saved);
    }

    @Transactional
    public GradeResponse update(Long id, Long institutionId, Long updatedBy, GradeRequest request) {
        Grade grade = findOrThrow(id, institutionId);
        String oldScore = grade.getScore().toString();

        grade.setScore(request.getScore());
        grade.setObservations(request.getObservations());
        grade.setPeriod(request.getPeriod());

        Grade saved = gradeRepository.save(grade);

        producer.sendGradeAudit(Map.of(
                "gradeId", saved.getId().toString(),
                "oldValue", oldScore,
                "newValue", saved.getScore().toString(),
                "userId", updatedBy.toString()
        ));

        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id, Long institutionId) {
        gradeRepository.delete(findOrThrow(id, institutionId));
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getReport(Long studentId, Long institutionId) {
        return gradeRepository.findByStudentIdAndInstitutionId(studentId, institutionId)
                .stream().map(this::toResponse).toList();
    }

    private Grade findOrThrow(Long id, Long institutionId) {
        return gradeRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", id));
    }

    private GradeResponse toResponse(Grade g) {
        return GradeResponse.builder()
                .id(g.getId())
                .studentId(g.getStudentId())
                .courseId(g.getCourseId())
                .period(g.getPeriod())
                .score(g.getScore())
                .observations(g.getObservations())
                .createdBy(g.getCreatedBy())
                .createdAt(g.getCreatedAt())
                .updatedAt(g.getUpdatedAt())
                .build();
    }
}
