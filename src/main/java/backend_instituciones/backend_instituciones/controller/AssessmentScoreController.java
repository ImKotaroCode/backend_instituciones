package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.AssessmentScoreSaveRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.CourseAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assessments/{assessmentId}/scores")
@RequiredArgsConstructor
public class AssessmentScoreController {

    private final CourseAssessmentService assessmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ESTUDIANTE','PADRE','ADMINISTRACION')")
    public ResponseEntity<?> list(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(
                assessmentService.getScores(TenantContext.getInstitutionId(), assessmentId));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ADMINISTRACION')")
    public ResponseEntity<?> save(@PathVariable Long assessmentId,
                                   @RequestBody AssessmentScoreSaveRequest request) {
        return ResponseEntity.ok(assessmentService.saveScores(
                TenantContext.getInstitutionId(), assessmentId,
                TenantContext.getUserId(), request));
    }
}
