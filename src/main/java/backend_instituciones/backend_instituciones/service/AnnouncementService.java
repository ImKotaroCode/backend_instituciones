package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.Announcement;
import backend_instituciones.backend_instituciones.domain.enums.Priority;
import backend_instituciones.backend_instituciones.dto.request.AnnouncementRequest;
import backend_instituciones.backend_instituciones.dto.response.AnnouncementResponse;
import backend_instituciones.backend_instituciones.dto.response.PageResponse;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.messaging.RabbitMQProducer;
import backend_instituciones.backend_instituciones.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final RabbitMQProducer producer;

    public PageResponse<AnnouncementResponse> list(Long institutionId, int page, int size) {
        return PageResponse.from(
                announcementRepository.findByInstitutionIdOrderByCreatedAtDesc(institutionId, PageRequest.of(page, size))
                        .map(this::toResponse));
    }

    public AnnouncementResponse get(Long id, Long institutionId) {
        return toResponse(findOrThrow(id, institutionId));
    }

    @Transactional
    public AnnouncementResponse create(Long institutionId, Long createdBy, AnnouncementRequest request) {
        LocalDateTime publishedAt = request.getScheduledAt() == null ? LocalDateTime.now() : null;

        Announcement announcement = Announcement.builder()
                .institutionId(institutionId)
                .title(request.getTitle())
                .content(request.getContent())
                .targetRoles(String.join(",", request.getTargetRoles()))
                .priority(request.getPriority())
                .scheduledAt(request.getScheduledAt())
                .publishedAt(publishedAt)
                .createdBy(createdBy)
                .build();

        Announcement saved = announcementRepository.save(announcement);

        if (request.getPriority() == Priority.HIGH && request.getScheduledAt() == null) {
            producer.sendAnnouncement(Map.of(
                    "announcementId", saved.getId().toString(),
                    "institutionId", institutionId.toString(),
                    "title", saved.getTitle(),
                    "content", saved.getContent(),
                    "targetRoles", saved.getTargetRoles()
            ));
        }

        return toResponse(saved);
    }

    @Transactional
    public AnnouncementResponse update(Long id, Long institutionId, AnnouncementRequest request) {
        Announcement a = findOrThrow(id, institutionId);
        a.setTitle(request.getTitle());
        a.setContent(request.getContent());
        a.setTargetRoles(String.join(",", request.getTargetRoles()));
        a.setPriority(request.getPriority());
        a.setScheduledAt(request.getScheduledAt());
        return toResponse(announcementRepository.save(a));
    }

    @Transactional
    public void delete(Long id, Long institutionId) {
        announcementRepository.delete(findOrThrow(id, institutionId));
    }

    private Announcement findOrThrow(Long id, Long institutionId) {
        return announcementRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", id));
    }

    private AnnouncementResponse toResponse(Announcement a) {
        List<String> roles = a.getTargetRoles() != null && !a.getTargetRoles().isBlank()
                ? Arrays.asList(a.getTargetRoles().split(","))
                : List.of();
        return AnnouncementResponse.builder()
                .id(a.getId())
                .institutionId(a.getInstitutionId())
                .title(a.getTitle())
                .content(a.getContent())
                .targetRoles(roles)
                .priority(a.getPriority())
                .scheduledAt(a.getScheduledAt())
                .publishedAt(a.getPublishedAt())
                .createdAt(a.getCreatedAt())
                .createdBy(a.getCreatedBy())
                .build();
    }
}
