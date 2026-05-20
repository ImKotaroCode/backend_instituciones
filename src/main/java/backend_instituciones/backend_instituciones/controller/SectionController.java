package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.SectionRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.SectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
public class SectionController {

    private final SectionService service;

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.list(TenantContext.getInstitutionId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id, TenantContext.getInstitutionId()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> create(@Valid @RequestBody SectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(TenantContext.getInstitutionId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody SectionRequest request) {
        return ResponseEntity.ok(service.update(id, TenantContext.getInstitutionId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id, TenantContext.getInstitutionId());
        return ResponseEntity.noContent().build();
    }
}
