package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.TaskSubmissionReviewRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.CourseTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/submissions")
@RequiredArgsConstructor
public class TaskSubmissionController {

    private final CourseTaskService courseTaskService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ESTUDIANTE','ADMINISTRACION')")
    public ResponseEntity<?> list(@PathVariable String taskId) {
        Long institutionId = TenantContext.getInstitutionId();
        Long resolvedId = courseTaskService.resolveTaskId(taskId, institutionId);
        return ResponseEntity.ok(courseTaskService.getSubmissions(institutionId, resolvedId));
    }

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ESTUDIANTE','ADMINISTRACION')")
    public ResponseEntity<?> submit(
            @PathVariable String taskId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String comment,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        Long institutionId = TenantContext.getInstitutionId();
        Long resolvedId = courseTaskService.resolveTaskId(taskId, institutionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                courseTaskService.submit(institutionId, resolvedId,
                        studentId, studentName, groupId, groupName, comment, files));
    }

    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ESTUDIANTE','ADMINISTRACION')")
    public ResponseEntity<?> submitJson(
            @PathVariable String taskId,
            @RequestBody java.util.Map<String, Object> body) {
        Long institutionId = TenantContext.getInstitutionId();
        Long resolvedId = courseTaskService.resolveTaskId(taskId, institutionId);
        Long studentId = body.get("studentId") != null
                ? Long.valueOf(body.get("studentId").toString()) : null;
        String studentName = (String) body.get("studentName");
        Long groupId = body.get("groupId") != null
                ? Long.valueOf(body.get("groupId").toString()) : null;
        String groupName = (String) body.get("groupName");
        String comment = (String) body.get("comment");
        return ResponseEntity.status(HttpStatus.CREATED).body(
                courseTaskService.submit(institutionId, resolvedId,
                        studentId, studentName, groupId, groupName, comment, null));
    }

    @PutMapping("/{submissionId}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ADMINISTRACION')")
    public ResponseEntity<?> review(
            @PathVariable String taskId,
            @PathVariable Long submissionId,
            @RequestBody TaskSubmissionReviewRequest request) {
        Long institutionId = TenantContext.getInstitutionId();
        Long resolvedId = courseTaskService.resolveTaskId(taskId, institutionId);
        return ResponseEntity.ok(courseTaskService.review(
                institutionId, resolvedId, submissionId,
                TenantContext.getUserId(), request));
    }
}
