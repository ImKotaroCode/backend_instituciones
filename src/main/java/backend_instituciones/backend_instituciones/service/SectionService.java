package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.Section;
import backend_instituciones.backend_instituciones.dto.request.SectionRequest;
import backend_instituciones.backend_instituciones.dto.response.SectionResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository repository;

    public List<SectionResponse> list(Long institutionId) {
        return repository.findByInstitutionIdOrderByNameAsc(institutionId)
                .stream().map(this::toResponse).toList();
    }

    public SectionResponse get(Long id, Long institutionId) {
        return toResponse(findOrThrow(id, institutionId));
    }

    @Transactional
    public SectionResponse create(Long institutionId, SectionRequest request) {
        if (repository.existsByNameAndInstitutionId(request.getName(), institutionId)) {
            throw new BusinessException("Section name already exists", HttpStatus.CONFLICT, "NAME_TAKEN");
        }
        Section entity = Section.builder()
                .institutionId(institutionId)
                .name(request.getName())
                .build();
        return toResponse(repository.save(entity));
    }

    @Transactional
    public SectionResponse update(Long id, Long institutionId, SectionRequest request) {
        Section entity = findOrThrow(id, institutionId);
        entity.setName(request.getName());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(Long id, Long institutionId) {
        Section entity = findOrThrow(id, institutionId);
        repository.delete(entity);
    }

    private Section findOrThrow(Long id, Long institutionId) {
        return repository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", id));
    }

    private SectionResponse toResponse(Section s) {
        return SectionResponse.builder()
                .id(s.getId())
                .institutionId(s.getInstitutionId())
                .name(s.getName())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
