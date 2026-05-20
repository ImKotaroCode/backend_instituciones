package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.AcademicYearRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.AcademicYearService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/academic-years")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROVEEDOR','ADMINISTRACION')")
public class AcademicYearController {

    private final AcademicYearService service;

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.list(TenantContext.getInstitutionId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id, TenantContext.getInstitutionId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('PROVEEDOR')")
    public ResponseEntity<?> create(@Valid @RequestBody AcademicYearRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(TenantContext.getInstitutionId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROVEEDOR')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody AcademicYearRequest request) {
        return ResponseEntity.ok(service.update(id, TenantContext.getInstitutionId(), request));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('PROVEEDOR')")
    public ResponseEntity<?> close(@PathVariable Long id) {
        return ResponseEntity.ok(service.close(id, TenantContext.getInstitutionId()));
    }

    @PatchMapping("/{id}/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> open(@PathVariable Long id) {
        return ResponseEntity.ok(service.open(id, TenantContext.getInstitutionId()));
    }
}
