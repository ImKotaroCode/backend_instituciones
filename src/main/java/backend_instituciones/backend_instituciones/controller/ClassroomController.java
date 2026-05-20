package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.ClassroomRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.ClassroomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/classrooms")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
public class ClassroomController {

    private final ClassroomService service;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String academicYear,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.list(TenantContext.getInstitutionId(), academicYear, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id, TenantContext.getInstitutionId()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> create(@Valid @RequestBody ClassroomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(TenantContext.getInstitutionId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody ClassroomRequest request) {
        return ResponseEntity.ok(service.update(id, TenantContext.getInstitutionId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id, TenantContext.getInstitutionId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{classroomId}/students")
    public ResponseEntity<?> listStudents(@PathVariable Long classroomId) {
        return ResponseEntity.ok(service.listStudents(classroomId, TenantContext.getInstitutionId()));
    }

    @PostMapping("/{classroomId}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<Void> addStudent(@PathVariable Long classroomId,
                                           @RequestBody Map<String, Long> body) {
        service.addStudent(classroomId, body.get("studentId"), TenantContext.getInstitutionId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{classroomId}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<Void> removeStudent(@PathVariable Long classroomId,
                                              @PathVariable Long studentId) {
        service.removeStudent(classroomId, studentId, TenantContext.getInstitutionId());
        return ResponseEntity.noContent().build();
    }
}
