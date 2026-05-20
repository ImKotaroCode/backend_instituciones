package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.CreateCourseRequest;
import backend_instituciones.backend_instituciones.dto.request.EnrollRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(courseService.list(TenantContext.getInstitutionId(), page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.get(id, TenantContext.getInstitutionId()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE','ADMINISTRACION')")
    public ResponseEntity<?> create(@Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.create(TenantContext.getInstitutionId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE','ADMINISTRACION')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.ok(courseService.update(id, TenantContext.getInstitutionId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id, TenantContext.getInstitutionId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enroll")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<Void> enroll(@PathVariable Long id,
                                       @Valid @RequestBody EnrollRequest request) {
        courseService.enroll(id, TenantContext.getInstitutionId(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/enroll/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<Void> unenroll(@PathVariable Long id, @PathVariable Long studentId) {
        courseService.unenroll(id, TenantContext.getInstitutionId(), studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<?> students(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("students", courseService.getStudents(id, TenantContext.getInstitutionId())));
    }

    @GetMapping("/{id}/materials")
    public ResponseEntity<?> getMaterials(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getMaterials(id, TenantContext.getInstitutionId()));
    }

    @PostMapping("/{id}/materials")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE','ADMINISTRACION')")
    public ResponseEntity<?> addMaterial(@PathVariable Long id,
                                         @RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                courseService.addMaterial(id, TenantContext.getInstitutionId(),
                        TenantContext.getUserId(), body.get("title"), body.get("fileUrl")));
    }

    @DeleteMapping("/{id}/materials/{materialId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE','ADMINISTRACION')")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id, @PathVariable Long materialId) {
        courseService.deleteMaterial(id, TenantContext.getInstitutionId(), materialId);
        return ResponseEntity.noContent().build();
    }
}
