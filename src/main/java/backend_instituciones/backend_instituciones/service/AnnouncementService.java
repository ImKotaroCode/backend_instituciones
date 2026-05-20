package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.Announcement;
import backend_instituciones.backend_instituciones.domain.entity.AnnouncementView;
import backend_instituciones.backend_instituciones.domain.entity.User;
import backend_instituciones.backend_instituciones.domain.enums.Priority;
import backend_instituciones.backend_instituciones.dto.response.AnnouncementResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.AnnouncementRepository;
import backend_instituciones.backend_instituciones.repository.AnnouncementViewRepository;
import backend_instituciones.backend_instituciones.repository.UserRepository;
import com.sksamuel.scrimage.ImmutableImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnnouncementService {

    private final AnnouncementRepository announcementRepo;
    private final AnnouncementViewRepository viewRepo;
    private final UserRepository userRepo;
    private final SupabaseStorageService storageService;
    private final SseService sseService;

    // ── Admin: list ───────────────────────────────────────────────────────────

    public Map<String, Object> list(Long institutionId, String monthKey) {
        List<Announcement> items = monthKey != null && !monthKey.isBlank()
                ? announcementRepo.findByInstitutionIdAndMonthKeyOrderByCreatedAtDesc(institutionId, monthKey)
                : announcementRepo.findByInstitutionIdOrderByCreatedAtDesc(institutionId);

        return Map.of("items", items.stream().map(a -> toResponse(a, null)).toList());
    }

    public AnnouncementResponse get(Long id, Long institutionId) {
        return toResponse(findOrThrow(id, institutionId), null);
    }

    // ── Admin: create ─────────────────────────────────────────────────────────

    @Transactional
    public AnnouncementResponse create(Long institutionId, Long createdBy,
                                        String kind,
                                        String title,
                                        String linkUrl,
                                        String priority,
                                        String status,
                                        String monthKey,
                                        List<String> targetRoles,
                                        MultipartFile file) {
        String resolvedKind = resolveKind(kind);
        if ("BANNER".equals(resolvedKind)) {
            validateBannerFile(file);
        }

        String resolvedStatus = resolveStatus(status);
        String mediaUrl  = null;
        String mediaType = null;
        String mediaName = null;

        if (file != null && !file.isEmpty()) {
            mediaUrl  = storageService.upload(file, "announcements");
            mediaType = detectMediaType(file.getContentType());
            mediaName = sanitizeFilename(file.getOriginalFilename(), mediaType);
        }

        String resolvedMonthKey = monthKey != null ? monthKey : YearMonth.now().toString();
        LocalDateTime publishedAt = "PUBLICADO".equals(resolvedStatus) ? LocalDateTime.now() : null;

        Announcement saved = announcementRepo.save(Announcement.builder()
                .institutionId(institutionId)
                .kind(resolvedKind)
                .title(title != null ? title : "")
                .linkUrl(linkUrl)
                .mediaUrl(mediaUrl)
                .mediaType(mediaType)
                .mediaName(mediaName)
                .targetRoles(joinRoles(targetRoles))
                .priority(parsePriority(priority))
                .monthKey(resolvedMonthKey)
                .status(resolvedStatus)
                .publishedAt(publishedAt)
                .createdBy(createdBy)
                .build());

        if ("PUBLICADO".equals(resolvedStatus)) {
            notifyTargetUsers(saved);
        }

        return toResponse(saved, null);
    }

    // ── Admin: update ─────────────────────────────────────────────────────────

    @Transactional
    public AnnouncementResponse update(Long id, Long institutionId,
                                        String kind,
                                        String title,
                                        String linkUrl,
                                        String priority,
                                        String status,
                                        String monthKey,
                                        List<String> targetRoles,
                                        MultipartFile file) {
        Announcement a = findOrThrow(id, institutionId);
        boolean wasPublished = "PUBLICADO".equals(a.getStatus());

        String resolvedKind = kind != null ? resolveKind(kind) : a.getKind();
        if ("BANNER".equals(resolvedKind) && file != null && !file.isEmpty()) {
            validateBannerFile(file);
        }

        String resolvedStatus = resolveStatus(status);

        if (file != null && !file.isEmpty()) {
            a.setMediaUrl(storageService.upload(file, "announcements"));
            a.setMediaType(detectMediaType(file.getContentType()));
            a.setMediaName(sanitizeFilename(file.getOriginalFilename(), a.getMediaType()));
        }

        a.setKind(resolvedKind);
        a.setTitle(title != null ? title : a.getTitle());
        a.setLinkUrl(linkUrl);
        a.setTargetRoles(joinRoles(targetRoles));
        a.setPriority(priority != null ? Priority.valueOf(priority) : a.getPriority());
        a.setMonthKey(monthKey != null ? monthKey : a.getMonthKey());
        a.setStatus(resolvedStatus);

        if (!wasPublished && "PUBLICADO".equals(resolvedStatus)) {
            a.setPublishedAt(LocalDateTime.now());
        }

        Announcement saved = announcementRepo.save(a);

        if (!wasPublished && "PUBLICADO".equals(resolvedStatus)) {
            notifyTargetUsers(saved);
        }

        return toResponse(saved, null);
    }

    // ── Admin: delete ─────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id, Long institutionId) {
        announcementRepo.delete(findOrThrow(id, institutionId));
    }

    // ── User: inbox ───────────────────────────────────────────────────────────

    public Map<String, Object> inbox(Long institutionId, String role, Long userId,
                                      String monthKey, String kind) {
        String resolvedMonthKey = monthKey != null && !monthKey.isBlank()
                ? monthKey
                : YearMonth.now().toString();
        String resolvedKind = kind != null && !kind.isBlank() ? kind.toUpperCase() : null;

        List<Announcement> announcements = announcementRepo.findInboxForRole(
                institutionId, role, resolvedMonthKey, resolvedKind);

        if (announcements.isEmpty()) return Map.of("items", List.of());

        // Sort: priority DESC (ALTA=2 first), then publishedAt DESC
        announcements = announcements.stream()
                .sorted(Comparator
                        .comparingInt((Announcement a) ->
                                a.getPriority() != null ? a.getPriority().ordinal() : -1)
                        .reversed()
                        .thenComparing(a ->
                                a.getPublishedAt() != null ? a.getPublishedAt() : LocalDateTime.MIN,
                                Comparator.reverseOrder()))
                .toList();

        // Batch-load views for this user
        List<Long> ids = announcements.stream().map(Announcement::getId).toList();
        Map<Long, AnnouncementView> viewMap = viewRepo
                .findByUserIdAndAnnouncementIdIn(userId, ids).stream()
                .collect(Collectors.toMap(AnnouncementView::getAnnouncementId, Function.identity()));

        List<AnnouncementResponse> items = announcements.stream()
                .map(a -> {
                    AnnouncementView v = viewMap.get(a.getId());
                    return toResponse(a, v != null ? v.getSeenAt() : null);
                })
                .toList();

        return Map.of("items", items);
    }

    // ── User: mark seen ───────────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> markSeen(Long announcementId, Long institutionId, Long userId) {
        // Validate announcement exists and belongs to institution
        findOrThrow(announcementId, institutionId);

        AnnouncementView view = viewRepo
                .findByAnnouncementIdAndUserId(announcementId, userId)
                .orElseGet(() -> AnnouncementView.builder()
                        .announcementId(announcementId)
                        .userId(userId)
                        .build());

        if (view.getSeenAt() == null) {
            view.setSeenAt(LocalDateTime.now());
            viewRepo.save(view);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("announcementId", announcementId);
        result.put("userId", userId);
        result.put("seenAt", view.getSeenAt().toString());
        return result;
    }

    // ── SSE ───────────────────────────────────────────────────────────────────

    private void notifyTargetUsers(Announcement a) {
        List<String> roles = parseRoles(a.getTargetRoles());
        if (roles.isEmpty()) return;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", a.getId());
        payload.put("type", "announcement");
        payload.put("kind", a.getKind() != null ? a.getKind() : "ANUNCIO");
        payload.put("title", a.getTitle());
        payload.put("linkUrl", a.getLinkUrl() != null ? a.getLinkUrl() : "");
        payload.put("mediaUrl", a.getMediaUrl() != null ? a.getMediaUrl() : "");
        payload.put("mediaType", a.getMediaType() != null ? a.getMediaType() : "");
        payload.put("mediaName", a.getMediaName() != null ? a.getMediaName() : "");
        payload.put("targetRoles", roles);
        payload.put("priority", a.getPriority() != null ? a.getPriority().name() : "NORMAL");
        payload.put("monthKey", a.getMonthKey() != null ? a.getMonthKey() : "");
        payload.put("publishedAt", a.getPublishedAt() != null ? a.getPublishedAt().toString() : "");
        payload.put("message", "Nuevo anuncio institucional");

        // Find all users whose role is in targetRoles and notify those connected via SSE
        roles.forEach(role -> {
            try {
                backend_instituciones.backend_instituciones.domain.enums.Role enumRole =
                        backend_instituciones.backend_instituciones.domain.enums.Role.valueOf(role.trim());
                List<User> users = userRepo.findByInstitutionIdAndRole(a.getInstitutionId(), enumRole);
                users.stream()
                        .filter(User::isActive)
                        .map(u -> u.getId().toString())
                        .forEach(uid -> sseService.sendToUser(uid, "announcement", payload));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown role in targetRoles: {}", role);
            }
        });
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String resolveKind(String kind) {
        if ("BANNER".equalsIgnoreCase(kind)) return "BANNER";
        return "ANUNCIO";
    }

    private void validateBannerFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Banner requires an image file",
                    HttpStatus.BAD_REQUEST, "BANNER_FILE_REQUIRED");
        }
        String mime = file.getContentType();
        if (mime == null || !mime.startsWith("image/")) {
            throw new BusinessException("Banner must be an image file",
                    HttpStatus.BAD_REQUEST, "BANNER_NOT_IMAGE");
        }
        try {
            ImmutableImage img = ImmutableImage.loader().fromBytes(file.getBytes());
            int w = img.awt().getWidth();
            int h = img.awt().getHeight();
            if (w != 1064 || h != 200) {
                throw new BusinessException(
                        "Banner must be exactly 1064x200 px (got " + w + "x" + h + ")",
                        HttpStatus.BAD_REQUEST, "BANNER_INVALID_SIZE");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Cannot read banner image: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST, "BANNER_READ_ERROR");
        }
    }

    private Announcement findOrThrow(Long id, Long institutionId) {
        return announcementRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", id));
    }

    private AnnouncementResponse toResponse(Announcement a, LocalDateTime seenAt) {
        return AnnouncementResponse.builder()
                .id(a.getId())
                .institutionId(a.getInstitutionId())
                .kind(a.getKind())
                .title(a.getTitle())
                .content(a.getContent())
                .linkUrl(a.getLinkUrl())
                .mediaUrl(a.getMediaUrl())
                .mediaType(a.getMediaType())
                .mediaName(a.getMediaName())
                .targetRoles(parseRoles(a.getTargetRoles()))
                .priority(a.getPriority())
                .monthKey(a.getMonthKey())
                .status(a.getStatus())
                .publishedAt(a.getPublishedAt())
                .scheduledAt(a.getScheduledAt())
                .createdAt(a.getCreatedAt())
                .createdBy(a.getCreatedBy())
                .seenAt(seenAt)
                .build();
    }

    private List<String> parseRoles(String targetRoles) {
        if (targetRoles == null || targetRoles.isBlank()) return List.of();
        return Arrays.stream(targetRoles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String joinRoles(List<String> roles) {
        return roles != null ? String.join(",", roles) : "";
    }

    private String resolveStatus(String status) {
        if (status == null) return "BORRADOR";
        return switch (status.toUpperCase()) {
            case "PUBLICADO"  -> "PUBLICADO";
            case "ARCHIVADO"  -> "ARCHIVADO";
            default           -> "BORRADOR";
        };
    }

    private String detectMediaType(String contentType) {
        if (contentType == null) return "IMAGE";
        return contentType.startsWith("video/") ? "VIDEO" : "IMAGE";
    }

    private Priority parsePriority(String priority) {
        if (priority == null || priority.isBlank()) return Priority.MEDIA;
        try {
            return Priority.valueOf(priority.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return Priority.MEDIA;
        }
    }

    private String sanitizeFilename(String original, String mediaType) {
        if (original == null) return "media." + (mediaType.equals("VIDEO") ? "mp4" : "webp");
        String base = original.contains(".")
                ? original.substring(0, original.lastIndexOf('.'))
                : original;
        String ext = "VIDEO".equals(mediaType) ? ".mp4" : ".webp";
        return base + ext;
    }
}
