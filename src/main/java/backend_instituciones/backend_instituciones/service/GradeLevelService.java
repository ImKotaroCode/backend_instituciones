package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.GradeLevel;
import backend_instituciones.backend_instituciones.dto.request.GradeLevelRequest;
import backend_instituciones.backend_instituciones.dto.response.GradeLevelResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.GradeLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeLevelService {

    private final GradeLevelRepository repository;

    public List<GradeLevelResponse> list(Long institutionId) {
        return repository.findByInstitutionIdOrderByOrderIndexAscNameAsc(institutionId)
                .stream().map(this::toResponse).toList();
    }

    public GradeLevelResponse get(Long id, Long institutionId) {
        return toResponse(findOrThrow(id, institutionId));
    }

    @Transactional
    public GradeLevelResponse create(Long institutionId, GradeLevelRequest request) {
        if (repository.existsByNameAndInstitutionId(request.getName(), institutionId)) {
            throw new BusinessException("Grade level name already exists", HttpStatus.CONFLICT, "NAME_TAKEN");
        }
        GradeLevel entity = GradeLevel.builder()
                .institutionId(institutionId)
                .name(request.getName())
                .orderIndex(request.getOrderIndex())
                .level(request.getLevel())
                .build();
        return toResponse(repository.save(entity));
    }

    @Transactional
    public GradeLevelResponse update(Long id, Long institutionId, GradeLevelRequest request) {
        GradeLevel entity = findOrThrow(id, institutionId);
        entity.setName(request.getName());
        entity.setOrderIndex(request.getOrderIndex());
        entity.setLevel(request.getLevel());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(Long id, Long institutionId) {
        GradeLevel entity = findOrThrow(id, institutionId);
        repository.delete(entity);
    }

    private GradeLevel findOrThrow(Long id, Long institutionId) {
        return repository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("GradeLevel", id));
    }

    private GradeLevelResponse toResponse(GradeLevel g) {
        return GradeLevelResponse.builder()
                .id(g.getId())
                .institutionId(g.getInstitutionId())
                .name(g.getName())
                .orderIndex(g.getOrderIndex())
                .level(g.getLevel())
                .createdAt(g.getCreatedAt())
                .build();
    }
}
