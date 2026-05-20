package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.CourseAssessment;
import backend_instituciones.backend_instituciones.domain.entity.CourseAssessmentScore;
import backend_instituciones.backend_instituciones.dto.request.AssessmentScoreSaveRequest;
import backend_instituciones.backend_instituciones.dto.request.CourseAssessmentRequest;
import backend_instituciones.backend_instituciones.dto.response.AssessmentScoreResponse;
import backend_instituciones.backend_instituciones.dto.response.CourseAssessmentResponse;
import backend_instituciones.backend_instituciones.dto.response.StudentAssessmentResponse;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.CourseAssessmentRepository;
import backend_instituciones.backend_instituciones.repository.CourseAssessmentScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseAssessmentService {

    private final CourseAssessmentRepository assessmentRepo;
    private final CourseAssessmentScoreRepository scoreRepo;
    private final CourseNotificationService courseNotificationService;

    // ── list ────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CourseAssessmentResponse> list(Long institutionId, Long courseId, Long periodId) {
        List<CourseAssessment> list = assessmentRepo.findByCourseIdAndInstitutionIdOrderByPublishedAtDesc(courseId, institutionId);
        if (periodId != null) {
            list = list.stream().filter(a -> periodId.equals(a.getPeriodId())).toList();
        }
        return list.stream().map(this::toResponse).toList();
    }

    // ── create ──────────────────────────────────────────────────────────────────

    @Transactional
    public CourseAssessmentResponse create(Long institutionId, Long courseId,
                                            Long createdBy, CourseAssessmentRequest request) {
        // Validate total weight does not exceed 100%
        BigDecimal newWeight = request.getWeightPercentage() != null
                ? request.getWeightPercentage() : BigDecimal.ZERO;

        // Weight sum check scoped to course + period (not global per course)
        List<CourseAssessment> siblings = assessmentRepo.findByCourseIdAndInstitutionId(courseId, institutionId)
                .stream()
                .filter(a -> {
                    if (request.getPeriodId() != null) return request.getPeriodId().equals(a.getPeriodId());
                    return request.getPeriod() != null && request.getPeriod().equals(a.getPeriod());
                })
                .toList();
        BigDecimal currentSum = siblings.stream()
                .map(CourseAssessment::getWeightPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (currentSum.add(newWeight).compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new backend_instituciones.backend_instituciones.exception.BusinessException(
                    "El peso total de los componentes supera el 100% en este periodo",
                    org.springframework.http.HttpStatus.BAD_REQUEST, "PESO_EXCEDIDO");
        }

        CourseAssessment assessment = CourseAssessment.builder()
                .institutionId(institutionId)
                .courseId(courseId)
                .title(request.getTitle())
                .description(request.getDescription())
                .period(request.getPeriod())
                .periodId(request.getPeriodId())
                .periodName(request.getPeriodName() != null ? request.getPeriodName() : request.getPeriod())
                .type(request.getType())
                .maxScore(request.getMaxScore() != null ? request.getMaxScore() : BigDecimal.valueOf(20))
                .passingScore(request.getPassingScore() != null ? request.getPassingScore() : BigDecimal.valueOf(11))
                .weightPercentage(newWeight)
                .createdBy(createdBy)
                .publishedAt(LocalDateTime.now())
                .build();

        CourseAssessmentResponse response = toResponse(assessmentRepo.save(assessment));

        // Notify enrolled students async
        courseNotificationService.notifyStudentsNewAssessment(
                institutionId, courseId,
                response.getId(), assessment.getTitle(), assessment.getDescription(),
                assessment.getPeriodId(), assessment.getPeriodName());

        return response;
    }

    // ── scores: list ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AssessmentScoreResponse> getScores(Long institutionId, Long assessmentId) {
        assessmentRepo.findByIdAndInstitutionId(assessmentId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluacion", assessmentId));
        return scoreRepo.findByAssessmentId(assessmentId).stream()
                .map(this::toScoreResponse).toList();
    }

    // ── scores: save bulk ───────────────────────────────────────────────────────

    @Transactional
    public List<AssessmentScoreResponse> saveScores(Long institutionId, Long assessmentId,
                                                     Long updatedBy, AssessmentScoreSaveRequest request) {
        CourseAssessment assessment = assessmentRepo.findByIdAndInstitutionId(assessmentId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluacion", assessmentId));

        Map<Long, CourseAssessmentScore> existing = scoreRepo.findByAssessmentId(assessmentId).stream()
                .collect(Collectors.toMap(CourseAssessmentScore::getStudentId, Function.identity()));

        List<CourseAssessmentScore> saved = request.getScores().stream()
                .filter(item -> item.getStudentId() != null)
                .map(item -> {
                    CourseAssessmentScore score = existing.getOrDefault(item.getStudentId(),
                            CourseAssessmentScore.builder()
                                    .assessmentId(assessmentId)
                                    .studentId(item.getStudentId())
                                    .build());
                    score.setStudentName(item.getStudentName());
                    score.setValue(item.getValue());
                    score.setFeedback(item.getFeedback());
                    score.setStatus(computeStatus(item.getValue(), assessment.getPassingScore()));
                    score.setUpdatedAt(LocalDateTime.now());
                    score.setUpdatedBy(updatedBy);
                    return scoreRepo.save(score);
                }).toList();

        return saved.stream().map(this::toScoreResponse).toList();
    }

    // ── student view ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<StudentAssessmentResponse> getStudentAssessments(Long institutionId,
                                                                   Long studentId, Long courseId) {
        List<CourseAssessment> assessments = assessmentRepo
                .findByCourseIdAndInstitutionIdOrderByPublishedAtDesc(courseId, institutionId);

        if (assessments.isEmpty()) return List.of();

        List<Long> assessmentIds = assessments.stream().map(CourseAssessment::getId).toList();
        Map<Long, CourseAssessmentScore> scoreMap = scoreRepo.findByAssessmentIdInAndStudentId(assessmentIds, studentId).stream()
                .collect(Collectors.toMap(CourseAssessmentScore::getAssessmentId, Function.identity()));

        return assessments.stream().map(a -> {
            CourseAssessmentScore sc = scoreMap.get(a.getId());
            StudentAssessmentResponse.ScoreSummary scoreSummary = sc != null
                    ? StudentAssessmentResponse.ScoreSummary.builder()
                            .value(sc.getValue())
                            .status(sc.getStatus())
                            .feedback(sc.getFeedback())
                            .build()
                    : null;

            return StudentAssessmentResponse.builder()
                    .id(a.getId())
                    .courseId(a.getCourseId())
                    .title(a.getTitle())
                    .description(a.getDescription())
                    .period(a.getPeriod())
                    .type(a.getType())
                    .maxScore(a.getMaxScore())
                    .passingScore(a.getPassingScore())
                    .publishedAt(a.getPublishedAt())
                    .score(scoreSummary)
                    .build();
        }).toList();
    }

    // ── helpers ─────────────────────────────────────────────────────────────────

    private String computeStatus(BigDecimal value, BigDecimal passingScore) {
        if (value == null) return "PENDIENTE";
        return value.compareTo(passingScore) >= 0 ? "APROBADO" : "DESAPROBADO";
    }

    private CourseAssessmentResponse toResponse(CourseAssessment a) {
        return CourseAssessmentResponse.builder()
                .id(a.getId())
                .institutionId(a.getInstitutionId())
                .courseId(a.getCourseId())
                .periodId(a.getPeriodId())
                .periodName(a.getPeriodName())
                .title(a.getTitle())
                .description(a.getDescription())
                .period(a.getPeriod())
                .type(a.getType())
                .maxScore(a.getMaxScore())
                .passingScore(a.getPassingScore())
                .weightPercentage(a.getWeightPercentage())
                .createdBy(a.getCreatedBy())
                .publishedAt(a.getPublishedAt())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private AssessmentScoreResponse toScoreResponse(CourseAssessmentScore s) {
        return AssessmentScoreResponse.builder()
                .id(s.getId())
                .assessmentId(s.getAssessmentId())
                .studentId(s.getStudentId())
                .studentName(s.getStudentName())
                .value(s.getValue())
                .status(s.getStatus())
                .feedback(s.getFeedback())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
