package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.AcademicGrade;
import backend_instituciones.backend_instituciones.domain.entity.AcademicLevel;
import backend_instituciones.backend_instituciones.domain.entity.AcademicSection;
import backend_instituciones.backend_instituciones.dto.request.AcademicStructureRequest;
import backend_instituciones.backend_instituciones.dto.response.AcademicStructureResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.AcademicGradeRepository;
import backend_instituciones.backend_instituciones.repository.AcademicLevelRepository;
import backend_instituciones.backend_instituciones.repository.AcademicSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AcademicStructureService {

    private final AcademicLevelRepository levelRepository;
    private final AcademicGradeRepository gradeRepository;
    private final AcademicSectionRepository sectionRepository;

    public AcademicStructureResponse getStructure(Long institutionId) {
        List<AcademicLevel> levels = levelRepository.findByInstitutionIdOrderBySortOrderAscNameAsc(institutionId);
        return toResponse(institutionId, levels);
    }

    @Transactional
    public AcademicStructureResponse putStructure(Long institutionId, AcademicStructureRequest request) {
        if (request.getLevels() == null) return getStructure(institutionId);

        for (AcademicStructureRequest.LevelRequest lr : request.getLevels()) {
            AcademicLevel level = upsertLevel(institutionId, lr);

            if (lr.getGrades() == null) continue;
            for (AcademicStructureRequest.GradeRequest gr : lr.getGrades()) {
                AcademicGrade grade = upsertGrade(level.getId(), gr);

                if (gr.getSections() == null) continue;
                for (AcademicStructureRequest.SectionRequest sr : gr.getSections()) {
                    upsertSection(grade.getId(), sr);
                }
            }
        }
        return getStructure(institutionId);
    }

    @Transactional
    public AcademicStructureResponse loadTemplatePerú(Long institutionId) {
        if (!levelRepository.findByInstitutionIdOrderBySortOrderAscNameAsc(institutionId).isEmpty()) {
            throw new BusinessException("Structure already exists. Clear it first or edit existing.", HttpStatus.CONFLICT, "ALREADY_EXISTS");
        }

        Map<String, String[]> template = Map.of(
                "Inicial", new String[]{"3 AÑOS", "4 AÑOS", "5 AÑOS"},
                "Primaria", new String[]{"1", "2", "3", "4", "5", "6"},
                "Secundaria", new String[]{"1", "2", "3", "4", "5"}
        );
        String[] sectionNames = {"A", "B", "C", "D"};
        int[] levelOrders = {1, 2, 3};
        String[] levelNames = {"Inicial", "Primaria", "Secundaria"};

        for (int li = 0; li < levelNames.length; li++) {
            String levelName = levelNames[li];
            AcademicLevel level = levelRepository.save(AcademicLevel.builder()
                    .institutionId(institutionId)
                    .name(levelName)
                    .code(generateLevelCode(levelName))
                    .sortOrder(levelOrders[li])
                    .status("ACTIVE")
                    .build());

            String[] grades = template.get(levelName);
            for (int gi = 0; gi < grades.length; gi++) {
                AcademicGrade grade = gradeRepository.save(AcademicGrade.builder()
                        .levelId(level.getId())
                        .name(grades[gi])
                        .sortOrder(gi + 1)
                        .status("ACTIVE")
                        .build());

                for (int si = 0; si < sectionNames.length; si++) {
                    sectionRepository.save(AcademicSection.builder()
                            .gradeId(grade.getId())
                            .name(sectionNames[si])
                            .sortOrder(si + 1)
                            .status("ACTIVE")
                            .build());
                }
            }
        }
        return getStructure(institutionId);
    }

    // ── Granular level CRUD ──────────────────────────────────────────────

    @Transactional
    public AcademicStructureResponse.LevelResponse createLevel(Long institutionId, AcademicStructureRequest.LevelRequest req) {
        if (req.getName() == null || req.getName().isBlank())
            throw new BusinessException("Level name required", HttpStatus.BAD_REQUEST, "INVALID");
        AcademicLevel level = levelRepository.save(AcademicLevel.builder()
                .institutionId(institutionId)
                .name(req.getName())
                .code(generateLevelCode(req.getName()))
                .sortOrder(req.getSortOrder())
                .status(req.getStatus() != null ? req.getStatus() : "ACTIVE")
                .build());
        return toLevelResponse(level, List.of());
    }

    @Transactional
    public AcademicStructureResponse.LevelResponse updateLevel(Long id, Long institutionId, AcademicStructureRequest.LevelRequest req) {
        AcademicLevel level = levelRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicLevel", id));
        if (req.getName() != null && !req.getName().isBlank()) {
            level.setName(req.getName());
            level.setCode(generateLevelCode(req.getName()));
        }
        if (req.getSortOrder() != null) level.setSortOrder(req.getSortOrder());
        if (req.getStatus() != null) level.setStatus(req.getStatus());
        level = levelRepository.save(level);
        List<AcademicGrade> grades = gradeRepository.findByLevelIdOrderBySortOrderAscNameAsc(level.getId());
        return toLevelResponse(level, grades);
    }

    @Transactional
    public void deleteLevel(Long id, Long institutionId) {
        AcademicLevel level = levelRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicLevel", id));
        List<AcademicGrade> grades = gradeRepository.findByLevelIdOrderBySortOrderAscNameAsc(level.getId());
        for (AcademicGrade g : grades) sectionRepository.deleteByGradeId(g.getId());
        gradeRepository.deleteByLevelId(level.getId());
        levelRepository.delete(level);
    }

    // ── Granular grade CRUD ──────────────────────────────────────────────

    @Transactional
    public AcademicStructureResponse.GradeResponse createGrade(Long levelId, Long institutionId, AcademicStructureRequest.GradeRequest req) {
        levelRepository.findByIdAndInstitutionId(levelId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicLevel", levelId));
        AcademicGrade grade = gradeRepository.save(AcademicGrade.builder()
                .levelId(levelId)
                .name(req.getName())
                .sortOrder(req.getSortOrder())
                .status(req.getStatus() != null ? req.getStatus() : "ACTIVE")
                .build());
        return toGradeResponse(grade, List.of());
    }

    @Transactional
    public AcademicStructureResponse.GradeResponse updateGrade(Long id, Long institutionId, AcademicStructureRequest.GradeRequest req) {
        AcademicGrade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicGrade", id));
        Long levelId = grade.getLevelId();
        levelRepository.findByIdAndInstitutionId(levelId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicLevel", levelId));
        if (req.getName() != null && !req.getName().isBlank()) grade.setName(req.getName());
        if (req.getSortOrder() != null) grade.setSortOrder(req.getSortOrder());
        if (req.getStatus() != null) grade.setStatus(req.getStatus());
        grade = gradeRepository.save(grade);
        List<AcademicSection> sections = sectionRepository.findByGradeIdOrderBySortOrderAscNameAsc(grade.getId());
        return toGradeResponse(grade, sections);
    }

    @Transactional
    public void deleteGrade(Long id, Long institutionId) {
        AcademicGrade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicGrade", id));
        levelRepository.findByIdAndInstitutionId(grade.getLevelId(), institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicLevel", grade.getLevelId()));
        sectionRepository.deleteByGradeId(grade.getId());
        gradeRepository.delete(grade);
    }

    // ── Granular section CRUD ────────────────────────────────────────────

    @Transactional
    public AcademicStructureResponse.SectionResponse createSection(Long gradeId, Long institutionId, AcademicStructureRequest.SectionRequest req) {
        AcademicGrade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicGrade", gradeId));
        levelRepository.findByIdAndInstitutionId(grade.getLevelId(), institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicLevel", grade.getLevelId()));
        AcademicSection section = sectionRepository.save(AcademicSection.builder()
                .gradeId(gradeId)
                .name(req.getName())
                .sortOrder(req.getSortOrder())
                .status(req.getStatus() != null ? req.getStatus() : "ACTIVE")
                .build());
        return toSectionResponse(section);
    }

    @Transactional
    public AcademicStructureResponse.SectionResponse updateSection(Long id, Long institutionId, AcademicStructureRequest.SectionRequest req) {
        AcademicSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicSection", id));
        AcademicGrade grade = gradeRepository.findById(section.getGradeId())
                .orElseThrow(() -> new ResourceNotFoundException("AcademicGrade", section.getGradeId()));
        levelRepository.findByIdAndInstitutionId(grade.getLevelId(), institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicLevel", grade.getLevelId()));
        if (req.getName() != null && !req.getName().isBlank()) section.setName(req.getName());
        if (req.getSortOrder() != null) section.setSortOrder(req.getSortOrder());
        if (req.getStatus() != null) section.setStatus(req.getStatus());
        return toSectionResponse(sectionRepository.save(section));
    }

    @Transactional
    public void deleteSection(Long id, Long institutionId) {
        AcademicSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicSection", id));
        AcademicGrade grade = gradeRepository.findById(section.getGradeId())
                .orElseThrow(() -> new ResourceNotFoundException("AcademicGrade", section.getGradeId()));
        levelRepository.findByIdAndInstitutionId(grade.getLevelId(), institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicLevel", grade.getLevelId()));
        sectionRepository.delete(section);
    }

    // ── Internal helpers ─────────────────────────────────────────────────

    private AcademicLevel upsertLevel(Long institutionId, AcademicStructureRequest.LevelRequest req) {
        if (req.getId() != null) {
            AcademicLevel existing = levelRepository.findByIdAndInstitutionId(req.getId(), institutionId).orElse(null);
            if (existing != null) {
                if (req.getName() != null) { existing.setName(req.getName()); existing.setCode(generateLevelCode(req.getName())); }
                if (req.getSortOrder() != null) existing.setSortOrder(req.getSortOrder());
                if (req.getStatus() != null) existing.setStatus(req.getStatus());
                return levelRepository.save(existing);
            }
        }
        return levelRepository.save(AcademicLevel.builder()
                .institutionId(institutionId)
                .name(req.getName())
                .code(generateLevelCode(req.getName()))
                .sortOrder(req.getSortOrder())
                .status(req.getStatus() != null ? req.getStatus() : "ACTIVE")
                .build());
    }

    private AcademicGrade upsertGrade(Long levelId, AcademicStructureRequest.GradeRequest req) {
        if (req.getId() != null) {
            AcademicGrade existing = gradeRepository.findByIdAndLevelId(req.getId(), levelId).orElse(null);
            if (existing != null) {
                if (req.getName() != null) existing.setName(req.getName());
                if (req.getSortOrder() != null) existing.setSortOrder(req.getSortOrder());
                if (req.getStatus() != null) existing.setStatus(req.getStatus());
                return gradeRepository.save(existing);
            }
        }
        return gradeRepository.save(AcademicGrade.builder()
                .levelId(levelId)
                .name(req.getName())
                .sortOrder(req.getSortOrder())
                .status(req.getStatus() != null ? req.getStatus() : "ACTIVE")
                .build());
    }

    private AcademicSection upsertSection(Long gradeId, AcademicStructureRequest.SectionRequest req) {
        if (req.getId() != null) {
            AcademicSection existing = sectionRepository.findByIdAndGradeId(req.getId(), gradeId).orElse(null);
            if (existing != null) {
                if (req.getName() != null) existing.setName(req.getName());
                if (req.getSortOrder() != null) existing.setSortOrder(req.getSortOrder());
                if (req.getStatus() != null) existing.setStatus(req.getStatus());
                return sectionRepository.save(existing);
            }
        }
        return sectionRepository.save(AcademicSection.builder()
                .gradeId(gradeId)
                .name(req.getName())
                .sortOrder(req.getSortOrder())
                .status(req.getStatus() != null ? req.getStatus() : "ACTIVE")
                .build());
    }

    String generateLevelCode(String name) {
        if (name == null) return "OTR";
        String upper = name.trim().toUpperCase();
        return switch (upper) {
            case "INICIAL" -> "INI";
            case "PRIMARIA" -> "PRI";
            case "SECUNDARIA" -> "SEC";
            default -> {
                String letters = upper.replaceAll("[^A-Z]", "");
                yield letters.length() >= 3 ? letters.substring(0, 3) : (letters.isBlank() ? "OTR" : letters);
            }
        };
    }

    private AcademicStructureResponse toResponse(Long institutionId, List<AcademicLevel> levels) {
        List<AcademicStructureResponse.LevelResponse> levelResponses = levels.stream().map(l -> {
            List<AcademicGrade> grades = gradeRepository.findByLevelIdOrderBySortOrderAscNameAsc(l.getId());
            return toLevelResponse(l, grades);
        }).toList();
        return AcademicStructureResponse.builder().levels(levelResponses).build();
    }

    private AcademicStructureResponse.LevelResponse toLevelResponse(AcademicLevel l, List<AcademicGrade> grades) {
        List<AcademicStructureResponse.GradeResponse> gradeResponses = grades.stream().map(g -> {
            List<AcademicSection> sections = sectionRepository.findByGradeIdOrderBySortOrderAscNameAsc(g.getId());
            return toGradeResponse(g, sections);
        }).toList();
        return AcademicStructureResponse.LevelResponse.builder()
                .id(l.getId()).name(l.getName()).code(l.getCode())
                .sortOrder(l.getSortOrder()).status(l.getStatus())
                .grades(gradeResponses).build();
    }

    private AcademicStructureResponse.GradeResponse toGradeResponse(AcademicGrade g, List<AcademicSection> sections) {
        List<AcademicStructureResponse.SectionResponse> sectionResponses = sections.stream()
                .map(this::toSectionResponse).toList();
        return AcademicStructureResponse.GradeResponse.builder()
                .id(g.getId()).name(g.getName())
                .sortOrder(g.getSortOrder()).status(g.getStatus())
                .sections(sectionResponses).build();
    }

    private AcademicStructureResponse.SectionResponse toSectionResponse(AcademicSection s) {
        return AcademicStructureResponse.SectionResponse.builder()
                .id(s.getId()).name(s.getName())
                .sortOrder(s.getSortOrder()).status(s.getStatus()).build();
    }
}
