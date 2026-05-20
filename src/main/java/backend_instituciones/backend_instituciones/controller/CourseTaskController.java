package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.CourseTaskRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.CourseTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/tasks")
@RequiredArgsConstructor
public class CourseTaskController {

    private final CourseTaskService courseTaskService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ESTUDIANTE','PADRE','ADMINISTRACION')")
    public ResponseEntity<?> list(@PathVariable Long courseId,
                                   @RequestParam(required = false) Long periodId) {
        return ResponseEntity.ok(
                courseTaskService.list(TenantContext.getInstitutionId(), courseId, periodId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ADMINISTRACION')")
    public ResponseEntity<?> create(@PathVariable Long courseId,
                                     @Valid @RequestBody CourseTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseTaskService.create(TenantContext.getInstitutionId(), courseId,
                        TenantContext.getUserId(), request));
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ADMINISTRACION')")
    public ResponseEntity<?> update(@PathVariable Long courseId,
                                     @PathVariable Long taskId,
                                     @Valid @RequestBody CourseTaskRequest request) {
        return ResponseEntity.ok(courseTaskService.update(
                TenantContext.getInstitutionId(), courseId, taskId, request));
    }
}
