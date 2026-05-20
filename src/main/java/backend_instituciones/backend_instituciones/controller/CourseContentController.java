package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.CourseContentPostRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.CourseContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/content")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE', 'ESTUDIANTE','ADMINISTRACION')")
public class CourseContentController {

    private final CourseContentService courseContentService;

    @GetMapping
    public ResponseEntity<?> list(@PathVariable Long courseId,
                                   @RequestParam(required = false) Long periodId) {
        return ResponseEntity.ok(
                courseContentService.list(TenantContext.getInstitutionId(), courseId, periodId));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(
            @PathVariable Long courseId,
            @RequestBody @Valid CourseContentPostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseContentService.create(TenantContext.getInstitutionId(), courseId, request, null));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createWithFiles(
            @PathVariable Long courseId,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("createdBy") Long createdBy,
            @RequestParam(value = "periodId", required = false) Long periodId,
            @RequestParam(value = "periodName", required = false) String periodName,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        CourseContentPostRequest request = new CourseContentPostRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCreatedBy(createdBy);
        request.setPeriodId(periodId);
        request.setPeriodName(periodName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseContentService.create(TenantContext.getInstitutionId(), courseId, request, files));
    }
}
