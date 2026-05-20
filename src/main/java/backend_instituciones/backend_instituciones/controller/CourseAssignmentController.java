package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.CourseAssignmentRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.CourseAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/course-assignments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'DOCENTE', 'ESTUDIANTE','ADMINISTRACION')")
public class CourseAssignmentController {

    private final CourseAssignmentService service;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Long classroomId,
                                  @RequestParam(required = false) String academicYear,
                                  @RequestParam(required = false) Long levelId,
                                  @RequestParam(required = false) Long gradeId,
                                  @RequestParam(required = false) Long sectionId,
                                  @RequestParam(required = false) Long teacherId,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.list(TenantContext.getInstitutionId(), classroomId,
                academicYear, levelId, gradeId, sectionId, teacherId, page, size));
    }

    @GetMapping("/classroom/{classroomId}")
    public ResponseEntity<?> listByClassroom(@PathVariable Long classroomId) {
        return ResponseEntity.ok(service.listByClassroom(classroomId, TenantContext.getInstitutionId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id, TenantContext.getInstitutionId()));
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<?> getStudents(@PathVariable Long id) {
        return ResponseEntity.ok(service.getStudents(id, TenantContext.getInstitutionId()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> create(@Valid @RequestBody CourseAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(TenantContext.getInstitutionId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody CourseAssignmentRequest request) {
        return ResponseEntity.ok(service.update(id, TenantContext.getInstitutionId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id, TenantContext.getInstitutionId());
        return ResponseEntity.noContent().build();
    }
}
