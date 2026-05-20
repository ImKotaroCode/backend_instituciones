package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.CourseContentAttachment;
import backend_instituciones.backend_instituciones.domain.entity.CourseContentPost;
import backend_instituciones.backend_instituciones.dto.request.CourseContentPostRequest;
import backend_instituciones.backend_instituciones.dto.response.CourseContentAttachmentResponse;
import backend_instituciones.backend_instituciones.dto.response.CourseContentPostResponse;
import backend_instituciones.backend_instituciones.repository.CourseContentAttachmentRepository;
import backend_instituciones.backend_instituciones.repository.CourseContentPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseContentService {

    private final CourseContentPostRepository courseContentPostRepository;
    private final CourseContentAttachmentRepository courseContentAttachmentRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final CourseNotificationService courseNotificationService;

    @Transactional(readOnly = true)
    public List<CourseContentPostResponse> list(Long institutionId, Long courseId, Long periodId) {
        List<CourseContentPost> posts =
                courseContentPostRepository.findByInstitutionIdAndCourseIdOrderByPublishedAtDesc(institutionId, courseId);
        if (periodId != null) {
            posts = posts.stream().filter(p -> periodId.equals(p.getPeriodId())).toList();
        }

        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream().map(CourseContentPost::getId).toList();
        List<CourseContentAttachment> allAttachments =
                courseContentAttachmentRepository.findByPostIdIn(postIds);

        Map<Long, List<CourseContentAttachment>> attachmentsByPostId = allAttachments.stream()
                .collect(Collectors.groupingBy(CourseContentAttachment::getPostId));

        return posts.stream()
                .map(post -> toResponse(post, attachmentsByPostId.getOrDefault(post.getId(), List.of())))
                .toList();
    }

    public CourseContentPostResponse create(Long institutionId, Long courseId,
                                            CourseContentPostRequest request,
                                            List<MultipartFile> files) {
        CourseContentPost post = CourseContentPost.builder()
                .institutionId(institutionId)
                .courseId(courseId)
                .createdBy(request.getCreatedBy())
                .title(request.getTitle())
                .description(request.getDescription())
                .periodId(request.getPeriodId())
                .periodName(request.getPeriodName())
                .publishedAt(LocalDateTime.now())
                .status("PUBLISHED")
                .build();

        CourseContentPost saved = courseContentPostRepository.save(post);
        List<CourseContentAttachment> attachments = new ArrayList<>();

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;
                String url = supabaseStorageService.upload(file, "course-content");
                CourseContentAttachment attachment = CourseContentAttachment.builder()
                        .postId(saved.getId())
                        .storageKey(url)
                        .fileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file")
                        .mimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                        .sizeBytes(file.getSize())
                        .build();
                attachments.add(courseContentAttachmentRepository.save(attachment));
            }
        }

        CourseContentPostResponse response = toResponse(saved, attachments);

        // Notify enrolled students async
        courseNotificationService.notifyStudentsNewContent(
                institutionId, courseId,
                saved.getId(), saved.getTitle(), saved.getDescription(),
                saved.getPeriodId(), saved.getPeriodName());

        return response;
    }

    public CourseContentPostResponse toResponse(CourseContentPost post,
                                                List<CourseContentAttachment> attachments) {
        List<CourseContentAttachmentResponse> attachmentResponses = attachments.stream()
                .map(a -> CourseContentAttachmentResponse.builder()
                        .id(a.getId())
                        .name(a.getFileName())
                        .url(a.getStorageKey())
                        .mimeType(a.getMimeType())
                        .sizeBytes(a.getSizeBytes())
                        .previewUrl(a.getPreviewStorageKey())
                        .build())
                .toList();

        return CourseContentPostResponse.builder()
                .id(post.getId())
                .courseId(post.getCourseId())
                .periodId(post.getPeriodId())
                .periodName(post.getPeriodName())
                .title(post.getTitle())
                .description(post.getDescription())
                .publishedAt(post.getPublishedAt())
                .createdBy(post.getCreatedBy())
                .status(post.getStatus())
                .attachments(attachmentResponses)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
