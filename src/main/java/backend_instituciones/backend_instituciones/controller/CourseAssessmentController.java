package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.CourseAssessmentRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.CourseAssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/assessments")
@RequiredArgsConstructor
public class CourseAssessmentController {

    private final CourseAssessmentService assessmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ESTUDIANTE','PADRE','ADMINISTRACION')")
    public ResponseEntity<?> list(@PathVariable Long courseId,
                                   @RequestParam(required = false) Long periodId) {
        return ResponseEntity.ok(
                assessmentService.list(TenantContext.getInstitutionId(), courseId, periodId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ADMINISTRACION')")
    public ResponseEntity<?> create(@PathVariable Long courseId,
                                     @Valid @RequestBody CourseAssessmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assessmentService.create(TenantContext.getInstitutionId(), courseId,
                        TenantContext.getUserId(), request));
    }
}
