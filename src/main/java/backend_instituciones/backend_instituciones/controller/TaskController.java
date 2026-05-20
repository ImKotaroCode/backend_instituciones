package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.CourseTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}")
@RequiredArgsConstructor
public class TaskController {

    private final CourseTaskService courseTaskService;

    @GetMapping("/submission-summary")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ADMINISTRACION')")
    public ResponseEntity<?> submissionSummary(@PathVariable String taskId) {
        return ResponseEntity.ok(
                courseTaskService.getSubmissionSummary(TenantContext.getInstitutionId(), taskId));
    }
}
