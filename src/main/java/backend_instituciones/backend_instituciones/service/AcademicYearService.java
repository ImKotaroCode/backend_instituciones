package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.AcademicYear;
import backend_instituciones.backend_instituciones.dto.request.AcademicYearRequest;
import backend_instituciones.backend_instituciones.dto.response.AcademicYearResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.AcademicYearRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicYearService {

    private final AcademicYearRepository repository;

    public List<AcademicYearResponse> list(Long institutionId) {
        return repository.findByInstitutionIdOrderByNameDesc(institutionId)
                .stream().map(this::toResponse).toList();
    }

    public AcademicYearResponse get(Long id, Long institutionId) {
        return toResponse(findOrThrow(id, institutionId));
    }

    @Transactional
    public AcademicYearResponse create(Long institutionId, AcademicYearRequest request) {
        if (repository.existsByNameAndInstitutionId(request.getName(), institutionId)) {
            throw new BusinessException("Academic year name already exists", HttpStatus.CONFLICT, "NAME_TAKEN");
        }
        if (Boolean.TRUE.equals(request.getIsCurrent())) {
            repository.clearCurrentForInstitution(institutionId);
        }
        AcademicYear entity = AcademicYear.builder()
                .institutionId(institutionId)
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isCurrent(Boolean.TRUE.equals(request.getIsCurrent()))
                .status("OPEN")
                .build();
        return toResponse(repository.save(entity));
    }

    @Transactional
    public AcademicYearResponse update(Long id, Long institutionId, AcademicYearRequest request) {
        AcademicYear entity = findOrThrow(id, institutionId);
        if (Boolean.TRUE.equals(request.getIsCurrent()) && !Boolean.TRUE.equals(entity.getIsCurrent())) {
            repository.clearCurrentForInstitution(institutionId);
        }
        entity.setName(request.getName());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        if (request.getIsCurrent() != null) entity.setIsCurrent(request.getIsCurrent());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public AcademicYearResponse close(Long id, Long institutionId) {
        AcademicYear entity = findOrThrow(id, institutionId);
        entity.setStatus("CLOSED");
        return toResponse(repository.save(entity));
    }

    @Transactional
    public AcademicYearResponse open(Long id, Long institutionId) {
        AcademicYear entity = findOrThrow(id, institutionId);
        entity.setStatus("OPEN");
        return toResponse(repository.save(entity));
    }

    public AcademicYear findOrThrow(Long id, Long institutionId) {
        return repository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", id));
    }

    private AcademicYearResponse toResponse(AcademicYear a) {
        return AcademicYearResponse.builder()
                .id(a.getId())
                .institutionId(a.getInstitutionId())
                .name(a.getName())
                .startDate(a.getStartDate())
                .endDate(a.getEndDate())
                .status(a.getStatus())
                .isCurrent(Boolean.TRUE.equals(a.getIsCurrent()))
                .createdAt(a.getCreatedAt())
                .build();
    }
}
