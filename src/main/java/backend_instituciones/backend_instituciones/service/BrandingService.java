package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.InstitutionConfig;
import backend_instituciones.backend_instituciones.dto.request.BrandingRequest;
import backend_instituciones.backend_instituciones.dto.response.BrandingResponse;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.InstitutionConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class BrandingService {

    private final InstitutionConfigRepository configRepository;

    @Cacheable(value = "institution:config", key = "#institutionId")
    public BrandingResponse getConfig(Long institutionId) {
        InstitutionConfig config = configRepository.findByInstitutionId(institutionId)
                .orElse(defaultConfig(institutionId));
        return toResponse(config);
    }

    @CacheEvict(value = "institution:config", key = "#institutionId")
    public BrandingResponse updateConfig(Long institutionId, BrandingRequest request) {
        InstitutionConfig config = configRepository.findByInstitutionId(institutionId)
                .orElseGet(() -> InstitutionConfig.builder().institutionId(institutionId).build());

        config.setName(request.getName());
        config.setPrimaryColor(request.getPrimaryColor());
        config.setSecondaryColor(request.getSecondaryColor());
        config.setBackgroundImage(request.getBackgroundImage());
        config.setLogoUrl(request.getLogoUrl());
        config.setFontFamily(request.getFontFamily());

        return toResponse(configRepository.save(config));
    }

    @CacheEvict(value = "institution:config", key = "#institutionId")
    public BrandingResponse updateLogoUrl(Long institutionId, String url) {
        InstitutionConfig config = configRepository.findByInstitutionId(institutionId)
                .orElseGet(() -> InstitutionConfig.builder().institutionId(institutionId)
                        .name("Institución").primaryColor("#1e293b")
                        .secondaryColor("#0ea5e9").fontFamily("Inter").build());
        config.setLogoUrl(url);
        return toResponse(configRepository.save(config));
    }

    @CacheEvict(value = "institution:config", key = "#institutionId")
    public BrandingResponse updateBackgroundUrl(Long institutionId, String url) {
        InstitutionConfig config = configRepository.findByInstitutionId(institutionId)
                .orElseGet(() -> InstitutionConfig.builder().institutionId(institutionId)
                        .name("Institución").primaryColor("#1e293b")
                        .secondaryColor("#0ea5e9").fontFamily("Inter").build());
        config.setBackgroundImage(url);
        return toResponse(configRepository.save(config));
    }

    private BrandingResponse toResponse(InstitutionConfig c) {
        return BrandingResponse.builder()
                .name(c.getName())
                .logoUrl(c.getLogoUrl())
                .primaryColor(c.getPrimaryColor())
                .secondaryColor(c.getSecondaryColor())
                .backgroundImage(c.getBackgroundImage())
                .fontFamily(c.getFontFamily())
                .build();
    }

    private InstitutionConfig defaultConfig(Long institutionId) {
        return InstitutionConfig.builder()
                .institutionId(institutionId)
                .name("Institución")
                .primaryColor("#1e293b")
                .secondaryColor("#0ea5e9")
                .fontFamily("Inter")
                .build();
    }
}
