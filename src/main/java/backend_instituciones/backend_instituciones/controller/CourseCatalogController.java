package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.CreateCourseRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/course-catalog")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'DOCENTE','ADMINISTRACION')")
public class CourseCatalogController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "200") int size) {
        return ResponseEntity.ok(courseService.list(TenantContext.getInstitutionId(), page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.get(id, TenantContext.getInstitutionId()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<?> create(@Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.create(TenantContext.getInstitutionId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.ok(courseService.update(id, TenantContext.getInstitutionId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id, TenantContext.getInstitutionId());
        return ResponseEntity.noContent().build();
    }
}
