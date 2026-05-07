package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.AcademicStructureRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.AcademicStructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/academic-structure")
@RequiredArgsConstructor
public class AcademicStructureController {

    private final AcademicStructureService service;

    @GetMapping
    public ResponseEntity<?> get() {
        return ResponseEntity.ok(service.getStructure(TenantContext.getInstitutionId()));
    }

    @PutMapping
    public ResponseEntity<?> put(@RequestBody AcademicStructureRequest request) {
        return ResponseEntity.ok(service.putStructure(TenantContext.getInstitutionId(), request));
    }

    @PostMapping("/load-template/peru")
    public ResponseEntity<?> loadTemplatePerú() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.loadTemplatePerú(TenantContext.getInstitutionId()));
    }

    // ── Levels ──────────────────────────────────────────────────────────

    @PostMapping("/levels")
    public ResponseEntity<?> createLevel(@RequestBody AcademicStructureRequest.LevelRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createLevel(TenantContext.getInstitutionId(), req));
    }

    @PutMapping("/levels/{id}")
    public ResponseEntity<?> updateLevel(@PathVariable Long id,
                                         @RequestBody AcademicStructureRequest.LevelRequest req) {
        return ResponseEntity.ok(service.updateLevel(id, TenantContext.getInstitutionId(), req));
    }

    @DeleteMapping("/levels/{id}")
    public ResponseEntity<?> deleteLevel(@PathVariable Long id) {
        service.deleteLevel(id, TenantContext.getInstitutionId());
        return ResponseEntity.noContent().build();
    }

    // ── Grades ──────────────────────────────────────────────────────────

    @PostMapping("/levels/{levelId}/grades")
    public ResponseEntity<?> createGrade(@PathVariable Long levelId,
                                         @RequestBody AcademicStructureRequest.GradeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createGrade(levelId, TenantContext.getInstitutionId(), req));
    }

    @PutMapping("/grades/{id}")
    public ResponseEntity<?> updateGrade(@PathVariable Long id,
                                         @RequestBody AcademicStructureRequest.GradeRequest req) {
        return ResponseEntity.ok(service.updateGrade(id, TenantContext.getInstitutionId(), req));
    }

    @DeleteMapping("/grades/{id}")
    public ResponseEntity<?> deleteGrade(@PathVariable Long id) {
        service.deleteGrade(id, TenantContext.getInstitutionId());
        return ResponseEntity.noContent().build();
    }

    // ── Sections ────────────────────────────────────────────────────────

    @PostMapping("/grades/{gradeId}/sections")
    public ResponseEntity<?> createSection(@PathVariable Long gradeId,
                                           @RequestBody AcademicStructureRequest.SectionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createSection(gradeId, TenantContext.getInstitutionId(), req));
    }

    @PutMapping("/sections/{id}")
    public ResponseEntity<?> updateSection(@PathVariable Long id,
                                           @RequestBody AcademicStructureRequest.SectionRequest req) {
        return ResponseEntity.ok(service.updateSection(id, TenantContext.getInstitutionId(), req));
    }

    @DeleteMapping("/sections/{id}")
    public ResponseEntity<?> deleteSection(@PathVariable Long id) {
        service.deleteSection(id, TenantContext.getInstitutionId());
        return ResponseEntity.noContent().build();
    }
}
