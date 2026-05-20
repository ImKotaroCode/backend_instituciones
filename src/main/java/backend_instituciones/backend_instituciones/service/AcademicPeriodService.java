package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.AcademicPeriod;
import backend_instituciones.backend_instituciones.dto.request.AcademicPeriodConfigRequest;
import backend_instituciones.backend_instituciones.dto.response.AcademicPeriodConfigResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.repository.AcademicPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicPeriodService {

    private final AcademicPeriodRepository periodRepo;

    @Transactional(readOnly = true)
    public AcademicPeriodConfigResponse getConfig(Long institutionId) {
        List<AcademicPeriod> periods = periodRepo.findByInstitutionIdOrderBySortOrderAsc(institutionId);
        String scheme = periods.isEmpty() ? null : periods.get(0).getScheme();
        return AcademicPeriodConfigResponse.builder()
                .scheme(scheme)
                .periods(periods.stream().map(this::toDto).toList())
                .build();
    }

    @Transactional
    public AcademicPeriodConfigResponse saveConfig(Long institutionId,
                                                    AcademicPeriodConfigRequest request) {
        List<AcademicPeriodConfigRequest.PeriodItem> items = request.getPeriods();

        // Validate: only one current
        long currentCount = items.stream().filter(AcademicPeriodConfigRequest.PeriodItem::isCurrent).count();
        if (currentCount > 1) {
            throw new BusinessException("Solo un periodo puede ser el actual",
                    HttpStatus.BAD_REQUEST, "MULTIPLE_CURRENT");
        }

        // Validate: startDate <= endDate and no overlaps among active periods
        List<AcademicPeriodConfigRequest.PeriodItem> active = items.stream()
                .filter(AcademicPeriodConfigRequest.PeriodItem::isActive).toList();

        for (AcademicPeriodConfigRequest.PeriodItem item : active) {
            if (item.getStartDate() != null && item.getEndDate() != null) {
                LocalDate start = LocalDate.parse(item.getStartDate());
                LocalDate end = LocalDate.parse(item.getEndDate());
                if (start.isAfter(end)) {
                    throw new BusinessException(
                            "Fecha inicio debe ser <= fecha fin en periodo: " + item.getName(),
                            HttpStatus.BAD_REQUEST, "FECHA_INVALIDA");
                }
            }
        }

        // Check overlaps
        for (int i = 0; i < active.size(); i++) {
            for (int j = i + 1; j < active.size(); j++) {
                AcademicPeriodConfigRequest.PeriodItem a = active.get(i);
                AcademicPeriodConfigRequest.PeriodItem b = active.get(j);
                if (a.getStartDate() != null && b.getStartDate() != null
                        && a.getEndDate() != null && b.getEndDate() != null) {
                    LocalDate aStart = LocalDate.parse(a.getStartDate());
                    LocalDate aEnd = LocalDate.parse(a.getEndDate());
                    LocalDate bStart = LocalDate.parse(b.getStartDate());
                    LocalDate bEnd = LocalDate.parse(b.getEndDate());
                    if (!aEnd.isBefore(bStart) && !bEnd.isBefore(aStart)) {
                        throw new BusinessException(
                                "Periodos se solapan: " + a.getName() + " y " + b.getName(),
                                HttpStatus.BAD_REQUEST, "PERIODOS_SOLAPADOS");
                    }
                }
            }
        }

        // Replace all periods
        periodRepo.deleteByInstitutionId(institutionId);

        List<AcademicPeriod> saved = items.stream().map(item -> periodRepo.save(
                AcademicPeriod.builder()
                        .institutionId(institutionId)
                        .scheme(request.getScheme())
                        .name(item.getName())
                        .code(item.getCode())
                        .startDate(item.getStartDate() != null ? LocalDate.parse(item.getStartDate()) : null)
                        .endDate(item.getEndDate() != null ? LocalDate.parse(item.getEndDate()) : null)
                        .sortOrder(item.getSortOrder())
                        .active(item.isActive())
                        .current(item.isCurrent())
                        .build()
        )).toList();

        return AcademicPeriodConfigResponse.builder()
                .scheme(request.getScheme())
                .periods(saved.stream().map(this::toDto).toList())
                .build();
    }

    private AcademicPeriodConfigResponse.PeriodDto toDto(AcademicPeriod p) {
        return AcademicPeriodConfigResponse.PeriodDto.builder()
                .id(p.getId())
                .name(p.getName())
                .code(p.getCode())
                .scheme(p.getScheme())
                .startDate(p.getStartDate() != null ? p.getStartDate().toString() : null)
                .endDate(p.getEndDate() != null ? p.getEndDate().toString() : null)
                .sortOrder(p.getSortOrder())
                .active(p.isActive())
                .current(p.isCurrent())
                .build();
    }
}
