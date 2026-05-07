package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.GradeRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE')")
    public ResponseEntity<?> getByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(gradeService.getByCourse(courseId, TenantContext.getInstitutionId()));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ESTUDIANTE','PADRE')")
    public ResponseEntity<?> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(gradeService.getByStudent(studentId, TenantContext.getInstitutionId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<?> create(@Valid @RequestBody GradeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gradeService.create(TenantContext.getInstitutionId(), TenantContext.getUserId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody GradeRequest request) {
        return ResponseEntity.ok(gradeService.update(id, TenantContext.getInstitutionId(), TenantContext.getUserId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        gradeService.delete(id, TenantContext.getInstitutionId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/report/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DOCENTE','ESTUDIANTE','PADRE')")
    public ResponseEntity<?> report(@PathVariable Long studentId) {
        return ResponseEntity.ok(gradeService.getReport(studentId, TenantContext.getInstitutionId()));
    }
}
