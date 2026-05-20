package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.*;
import backend_instituciones.backend_instituciones.dto.request.CourseTaskRequest;
import backend_instituciones.backend_instituciones.dto.request.TaskSubmissionReviewRequest;
import backend_instituciones.backend_instituciones.dto.response.*;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import backend_instituciones.backend_instituciones.dto.response.StudentCourseTaskOverviewResponse;

@Service
@RequiredArgsConstructor
public class CourseTaskService {

    private final CourseTaskRepository taskRepo;
    private final CourseTaskAllowedFormatRepository formatRepo;
    private final CourseTaskGroupRepository groupRepo;
    private final CourseTaskGroupMemberRepository memberRepo;
    private final CourseTaskSubmissionRepository submissionRepo;
    private final CourseTaskSubmissionFileRepository fileRepo;
    private final SupabaseStorageService storageService;
    private final CourseNotificationService courseNotificationService;

    private static final Map<String, String> MIME_TYPES = Map.ofEntries(
            Map.entry("pdf",  "application/pdf"),
            Map.entry("doc",  "application/msword"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("ppt",  "application/vnd.ms-powerpoint"),
            Map.entry("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
            Map.entry("jpg",  "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("png",  "image/png"),
            Map.entry("webp", "image/webp"),
            Map.entry("mp4",  "video/mp4"),
            Map.entry("rar",  "application/vnd.rar"),
            Map.entry("zip",  "application/zip")
    );

    // ── list ────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CourseTaskResponse> list(Long institutionId, Long courseId, Long periodId) {
        List<CourseTask> tasks = taskRepo.findByCourseIdAndInstitutionIdOrderByCreatedAtDesc(courseId, institutionId);
        if (periodId != null) {
            tasks = tasks.stream().filter(t -> periodId.equals(t.getPeriodId())).toList();
        }
        return toBatchResponses(tasks);
    }

    // ── create ──────────────────────────────────────────────────────────────────

    @Transactional
    public CourseTaskResponse create(Long institutionId, Long courseId, Long createdBy,
                                     CourseTaskRequest request) {
        CourseTask task = CourseTask.builder()
                .institutionId(institutionId)
                .courseId(courseId)
                .periodId(request.getPeriodId())
                .periodName(request.getPeriodName())
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .visibleFrom(request.getVisibleFrom())
                .dueAt(request.getDueAt())
                .maxScore(request.getMaxScore() != null ? request.getMaxScore() : BigDecimal.valueOf(20))
                .maxAttempts(request.getMaxAttempts() > 0 ? request.getMaxAttempts() : 1)
                .allowLateSubmission(request.isAllowLateSubmission())
                .groupTask(request.isGroupTask())
                .status(request.getStatus() != null ? request.getStatus() : "BORRADOR")
                .createdBy(createdBy)
                .build();

        task.setGeneratedCode(generateTaskCode());
        task = taskRepo.save(task);
        saveFormats(task.getId(), request.getAcceptedFormats());
        if (request.isGroupTask()) saveGroups(task.getId(), request.getGroups());

        CourseTaskResponse response = toBatchResponses(List.of(task)).get(0);

        // Notify enrolled students async (only if published)
        if ("PUBLICADO".equals(task.getStatus())) {
            courseNotificationService.notifyStudentsNewTask(
                    institutionId, courseId,
                    task.getId(), task.getTitle(), task.getDescription(),
                    task.getDueAt(), task.getPeriodId(), task.getPeriodName());
        }

        return response;
    }

    /** Resolve string taskId (numeric or generatedCode) → CourseTask */
    public CourseTask resolveTask(String taskRef, Long institutionId) {
        try {
            long numericId = Long.parseLong(taskRef);
            return taskRepo.findByIdAndInstitutionId(numericId, institutionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tarea", numericId));
        } catch (NumberFormatException e) {
            return taskRepo.findByGeneratedCodeAndInstitutionId(taskRef, institutionId)
                    .orElseThrow(() -> new BusinessException(
                            "Tarea no encontrada: " + taskRef, HttpStatus.NOT_FOUND, "NOT_FOUND"));
        }
    }

    /** Resolve to Long taskId */
    public Long resolveTaskId(String taskRef, Long institutionId) {
        return resolveTask(taskRef, institutionId).getId();
    }

    // ── update ──────────────────────────────────────────────────────────────────

    @Transactional
    public CourseTaskResponse update(Long institutionId, Long courseId, Long taskId,
                                     CourseTaskRequest request) {
        CourseTask task = taskRepo.findByIdAndInstitutionId(taskId, institutionId)
                .filter(t -> t.getCourseId().equals(courseId))
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", taskId));

        task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        task.setVisibleFrom(request.getVisibleFrom());
        task.setDueAt(request.getDueAt());
        if (request.getMaxScore() != null) task.setMaxScore(request.getMaxScore());
        task.setAllowLateSubmission(request.isAllowLateSubmission());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        task = taskRepo.save(task);

        formatRepo.deleteByTaskId(taskId);
        saveFormats(taskId, request.getAcceptedFormats());

        if ("PUBLICADO".equals(task.getStatus())) {
            courseNotificationService.notifyStudentsNewTask(
                    institutionId, courseId,
                    task.getId(), task.getTitle(), task.getDescription(),
                    task.getDueAt(), task.getPeriodId(), task.getPeriodName());
        }

        if (task.isGroupTask() && request.getGroups() != null) {
            List<CourseTaskGroup> oldGroups = groupRepo.findByTaskId(taskId);
            if (!oldGroups.isEmpty()) {
                memberRepo.deleteByGroupIdIn(oldGroups.stream().map(CourseTaskGroup::getId).toList());
                groupRepo.deleteByTaskId(taskId);
            }
            saveGroups(taskId, request.getGroups());
        }

        return toBatchResponses(List.of(task)).get(0);
    }

    // ── submissions: list ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TaskSubmissionResponse> getSubmissions(Long institutionId, Long taskId) {
        taskRepo.findByIdAndInstitutionId(taskId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", taskId));

        List<CourseTaskSubmission> submissions = submissionRepo.findByTaskId(taskId);
        return toSubmissionBatch(submissions);
    }

    // ── submissions: create ─────────────────────────────────────────────────────

    @Transactional
    public TaskSubmissionResponse submit(Long institutionId, Long taskId,
                                         Long studentId, String studentName,
                                         Long groupId, String groupName,
                                         String comment,
                                         List<MultipartFile> files) {
        CourseTask task = taskRepo.findByIdAndInstitutionId(taskId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", taskId));

        if ("CERRADO".equals(task.getStatus())) {
            throw new BusinessException("La tarea está cerrada y no acepta más entregas",
                    HttpStatus.BAD_REQUEST, "TAREA_CERRADA");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean late = now.isAfter(task.getDueAt());

        if (late && !task.isAllowLateSubmission()) {
            throw new BusinessException("La tarea está cerrada y no acepta entregas tardías",
                    HttpStatus.BAD_REQUEST, "ENTREGA_FUERA_DE_PLAZO");
        }

        // Validar intentos
        int existingAttempts;
        if (groupId != null) {
            existingAttempts = submissionRepo.findByTaskIdAndGroupId(taskId, groupId).size();
        } else if (studentId != null) {
            existingAttempts = submissionRepo.findByTaskIdAndStudentId(taskId, studentId).size();
        } else {
            existingAttempts = 0;
        }
        if (existingAttempts >= task.getMaxAttempts()) {
            throw new BusinessException(
                    "Has alcanzado el número máximo de intentos para esta tarea",
                    HttpStatus.BAD_REQUEST, "MAX_INTENTOS_ALCANZADOS");
        }
        int attemptNumber = existingAttempts + 1;

        // Validar grupo si aplica
        if (task.isGroupTask() && groupId != null) {
            List<CourseTaskGroup> taskGroups = groupRepo.findByTaskId(taskId);
            boolean groupBelongsToTask = taskGroups.stream().anyMatch(g -> g.getId().equals(groupId));
            if (!groupBelongsToTask) {
                throw new BusinessException("El grupo no pertenece a esta tarea",
                        HttpStatus.FORBIDDEN, "GRUPO_INVALIDO");
            }
            if (studentId != null && !memberRepo.existsByGroupIdAndStudentId(groupId, studentId)) {
                throw new BusinessException("El alumno no pertenece al grupo de la tarea",
                        HttpStatus.FORBIDDEN, "ALUMNO_FUERA_DE_GRUPO");
            }
        }

        String status = late ? "TARDIO" : "ENVIADO";

        CourseTaskSubmission submission = CourseTaskSubmission.builder()
                .institutionId(institutionId)
                .taskId(taskId)
                .courseId(task.getCourseId())
                .studentId(studentId)
                .studentName(studentName)
                .groupId(groupId)
                .groupName(groupName)
                .comment(comment)
                .submittedAt(now)
                .status(status)
                .attemptNumber(attemptNumber)
                .build();

        submission = submissionRepo.save(submission);

        List<CourseTaskSubmissionFile> savedFiles = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;
                String url = storageService.upload(file, "task-submissions");
                savedFiles.add(fileRepo.save(CourseTaskSubmissionFile.builder()
                        .submissionId(submission.getId())
                        .fileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "archivo")
                        .fileUrl(url)
                        .storageKey(url)
                        .mimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                        .sizeBytes(file.getSize())
                        .build()));
            }
        }

        return toSubmissionResponse(submission, savedFiles);
    }

    // ── submissions: review ─────────────────────────────────────────────────────

    @Transactional
    public TaskSubmissionResponse review(Long institutionId, Long taskId, Long submissionId,
                                          Long reviewedBy, TaskSubmissionReviewRequest request) {
        CourseTask task = taskRepo.findByIdAndInstitutionId(taskId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", taskId));

        CourseTaskSubmission submission = submissionRepo.findByIdAndInstitutionId(submissionId, institutionId)
                .filter(s -> s.getTaskId().equals(taskId))
                .orElseThrow(() -> new ResourceNotFoundException("Entrega", submissionId));

        if (request.getScore() != null) {
            if (request.getScore().compareTo(BigDecimal.ZERO) < 0
                    || request.getScore().compareTo(task.getMaxScore()) > 0) {
                throw new BusinessException(
                        "La nota debe estar entre 0 y " + task.getMaxScore(),
                        HttpStatus.BAD_REQUEST, "NOTA_INVALIDA");
            }
            submission.setScore(request.getScore());
        }
        if (request.getFeedback() != null) submission.setFeedback(request.getFeedback());
        submission.setStatus("REVISADO");
        submission.setReviewedBy(reviewedBy);
        submission.setReviewedAt(LocalDateTime.now());

        submission = submissionRepo.save(submission);

        // Notify student their submission was reviewed
        if (submission.getStudentId() != null) {
            courseNotificationService.notifySubmissionReviewed(
                    submission.getStudentId(), submissionId, taskId,
                    task.getCourseId(), institutionId,
                    submission.getScore(), submission.getFeedback(), reviewedBy);
        }

        List<CourseTaskSubmissionFile> files = fileRepo.findBySubmissionId(submissionId);
        return toSubmissionResponse(submission, files);
    }

    // ── student view ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<StudentTaskResponse> getStudentTasks(Long institutionId, Long studentId, Long courseId) {
        List<CourseTask> tasks = taskRepo.findByCourseIdAndInstitutionIdOrderByCreatedAtDesc(courseId, institutionId)
                .stream()
                .filter(t -> "PUBLICADO".equals(t.getStatus()))
                .toList();

        if (tasks.isEmpty()) return List.of();

        List<Long> taskIds = tasks.stream().map(CourseTask::getId).toList();

        // Batch load formats
        Map<Long, List<String>> formatsMap = formatRepo.findByTaskIdIn(taskIds).stream()
                .collect(Collectors.groupingBy(CourseTaskAllowedFormat::getTaskId,
                        Collectors.mapping(CourseTaskAllowedFormat::getExtension, Collectors.toList())));

        // Batch load groups
        Map<Long, List<CourseTaskGroup>> groupsMap = groupRepo.findByTaskIdIn(taskIds).stream()
                .collect(Collectors.groupingBy(CourseTaskGroup::getTaskId));

        // Find which groups the student belongs to (DB-filtered)
        List<Long> allGroupIds = groupsMap.values().stream()
                .flatMap(List::stream).map(CourseTaskGroup::getId).toList();
        Set<Long> myGroupIds = allGroupIds.isEmpty() ? Set.of()
                : memberRepo.findByGroupIdInAndStudentId(allGroupIds, studentId).stream()
                        .map(CourseTaskGroupMember::getGroupId)
                        .collect(Collectors.toSet());

        // Batch load submissions for this student (DB-filtered)
        List<CourseTaskSubmission> mySubmissions = new ArrayList<>(
                submissionRepo.findByTaskIdInAndStudentId(taskIds, studentId));
        if (!myGroupIds.isEmpty()) {
            mySubmissions.addAll(submissionRepo.findByTaskIdInAndGroupIdIn(taskIds, new ArrayList<>(myGroupIds)));
        }
        Map<Long, CourseTaskSubmission> submissionByTask = mySubmissions.stream()
                .collect(Collectors.toMap(CourseTaskSubmission::getTaskId, Function.identity(),
                        (a, b) -> a));

        List<Long> subIds = mySubmissions.stream().map(CourseTaskSubmission::getId).toList();
        Map<Long, List<CourseTaskSubmissionFile>> filesBySubmission = subIds.isEmpty() ? Map.of()
                : fileRepo.findBySubmissionIdIn(subIds).stream()
                        .collect(Collectors.groupingBy(CourseTaskSubmissionFile::getSubmissionId));

        // Build group map per task for student
        Map<Long, CourseTaskGroup> myGroupByTask = new HashMap<>();
        groupsMap.forEach((tid, groups) -> groups.forEach(g -> {
            if (myGroupIds.contains(g.getId())) myGroupByTask.put(tid, g);
        }));

        return tasks.stream().map(t -> {
            CourseTaskSubmission sub = submissionByTask.get(t.getId());
            CourseTaskGroup myGroup = myGroupByTask.get(t.getId());

            StudentTaskResponse.SubmissionSummary subSummary = null;
            if (sub != null) {
                List<CourseTaskSubmissionFile> subFiles = filesBySubmission.getOrDefault(sub.getId(), List.of());
                subSummary = StudentTaskResponse.SubmissionSummary.builder()
                        .id(sub.getId())
                        .status(sub.getStatus())
                        .score(sub.getScore())
                        .feedback(sub.getFeedback())
                        .submittedAt(sub.getSubmittedAt())
                        .attachments(subFiles.stream().map(this::toFileResponse).toList())
                        .build();
            }

            return StudentTaskResponse.builder()
                    .id(t.getId())
                    .courseId(t.getCourseId())
                    .title(t.getTitle())
                    .description(t.getDescription())
                    .category(t.getCategory())
                    .visibleFrom(t.getVisibleFrom())
                    .dueAt(t.getDueAt())
                    .maxScore(t.getMaxScore())
                    .allowLateSubmission(t.isAllowLateSubmission())
                    .groupTask(t.isGroupTask())
                    .acceptedFormats(formatsMap.getOrDefault(t.getId(), List.of()))
                    .status(t.getStatus())
                    .myGroup(myGroup != null
                            ? StudentTaskResponse.MyGroupInfo.builder()
                                    .id(myGroup.getId()).name(myGroup.getName()).build()
                            : null)
                    .submission(subSummary)
                    .build();
        }).toList();
    }

    // ── batch helpers ────────────────────────────────────────────────────────────

    private List<CourseTaskResponse> toBatchResponses(List<CourseTask> tasks) {
        if (tasks.isEmpty()) return List.of();

        List<Long> taskIds = tasks.stream().map(CourseTask::getId).toList();

        Map<Long, List<String>> formatsMap = formatRepo.findByTaskIdIn(taskIds).stream()
                .collect(Collectors.groupingBy(CourseTaskAllowedFormat::getTaskId,
                        Collectors.mapping(CourseTaskAllowedFormat::getExtension, Collectors.toList())));

        List<CourseTaskGroup> allGroups = groupRepo.findByTaskIdIn(taskIds);
        Map<Long, List<CourseTaskGroup>> groupsMap = allGroups.stream()
                .collect(Collectors.groupingBy(CourseTaskGroup::getTaskId));

        List<Long> groupIds = allGroups.stream().map(CourseTaskGroup::getId).toList();
        Map<Long, List<Long>> membersByGroup = groupIds.isEmpty() ? Map.of()
                : memberRepo.findByGroupIdIn(groupIds).stream()
                        .collect(Collectors.groupingBy(CourseTaskGroupMember::getGroupId,
                                Collectors.mapping(CourseTaskGroupMember::getStudentId, Collectors.toList())));

        return tasks.stream().map(t -> CourseTaskResponse.builder()
                .id(t.getId())
                .generatedCode(t.getGeneratedCode())
                .institutionId(t.getInstitutionId())
                .courseId(t.getCourseId())
                .periodId(t.getPeriodId())
                .periodName(t.getPeriodName())
                .title(t.getTitle())
                .description(t.getDescription())
                .category(t.getCategory())
                .visibleFrom(t.getVisibleFrom())
                .dueAt(t.getDueAt())
                .maxScore(t.getMaxScore())
                .maxAttempts(t.getMaxAttempts())
                .allowLateSubmission(t.isAllowLateSubmission())
                .groupTask(t.isGroupTask())
                .status(t.getStatus())
                .createdBy(t.getCreatedBy())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .acceptedFormats(formatsMap.getOrDefault(t.getId(), List.of()))
                .groups(groupsMap.getOrDefault(t.getId(), List.of()).stream()
                        .map(g -> TaskGroupResponse.builder()
                                .id(g.getId())
                                .name(g.getName())
                                .studentIds(membersByGroup.getOrDefault(g.getId(), List.of()))
                                .build())
                        .toList())
                .build()).toList();
    }

    private List<TaskSubmissionResponse> toSubmissionBatch(List<CourseTaskSubmission> submissions) {
        if (submissions.isEmpty()) return List.of();
        List<Long> ids = submissions.stream().map(CourseTaskSubmission::getId).toList();
        Map<Long, List<CourseTaskSubmissionFile>> filesMap = fileRepo.findBySubmissionIdIn(ids).stream()
                .collect(Collectors.groupingBy(CourseTaskSubmissionFile::getSubmissionId));
        return submissions.stream()
                .map(s -> toSubmissionResponse(s, filesMap.getOrDefault(s.getId(), List.of())))
                .toList();
    }

    private TaskSubmissionResponse toSubmissionResponse(CourseTaskSubmission s,
                                                         List<CourseTaskSubmissionFile> files) {
        return TaskSubmissionResponse.builder()
                .id(s.getId())
                .taskId(s.getTaskId())
                .courseId(s.getCourseId())
                .attemptNumber(s.getAttemptNumber())
                .groupId(s.getGroupId())
                .groupName(s.getGroupName())
                .studentId(s.getStudentId())
                .studentName(s.getStudentName())
                .submittedAt(s.getSubmittedAt())
                .status(s.getStatus())
                .comment(s.getComment())
                .score(s.getScore())
                .feedback(s.getFeedback())
                .reviewedBy(s.getReviewedBy())
                .reviewedAt(s.getReviewedAt())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .attachments(files.stream().map(this::toFileResponse).toList())
                .build();
    }

    private TaskSubmissionFileResponse toFileResponse(CourseTaskSubmissionFile f) {
        return TaskSubmissionFileResponse.builder()
                .id(f.getId())
                .name(f.getFileName())
                .url(f.getFileUrl())
                .mimeType(f.getMimeType())
                .sizeBytes(f.getSizeBytes())
                .previewUrl(f.getPreviewUrl())
                .build();
    }

    private void saveFormats(Long taskId, List<String> extensions) {
        if (extensions == null || extensions.isEmpty()) return;
        extensions.forEach(ext -> {
            String clean = ext.toLowerCase().trim();
            formatRepo.save(CourseTaskAllowedFormat.builder()
                    .taskId(taskId)
                    .extension(clean)
                    .mimeType(MIME_TYPES.getOrDefault(clean, null))
                    .build());
        });
    }

    private String generateTaskCode() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        java.util.Random rnd = new java.util.Random();
        int attempts = 0;
        while (attempts++ < 20) {
            StringBuilder sb = new StringBuilder("task-");
            for (int i = 0; i < 8; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
            sb.append("-");
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
            String code = sb.toString();
            if (!taskRepo.existsByGeneratedCode(code)) return code;
        }
        return "task-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 14);
    }

    // ── student overview: all tasks + submission state ─────────────────────────

    @Transactional(readOnly = true)
    public StudentCourseTaskOverviewResponse getCourseTaskOverview(
            Long institutionId, Long studentId, Long courseId,
            Long periodId, String statusFilter) {

        // Exclude only BORRADOR — students must see PUBLICADO and CERRADO (closed tasks may have submissions)
        List<CourseTask> tasks = taskRepo.findByCourseIdAndInstitutionIdOrderByCreatedAtDesc(courseId, institutionId)
                .stream()
                .filter(t -> !"BORRADOR".equals(t.getStatus()))
                .toList();

        if (periodId != null) {
            tasks = tasks.stream().filter(t -> periodId.equals(t.getPeriodId())).toList();
        }

        // Status filter: ACTIVAS / PASADAS / TODAS
        if (statusFilter != null && !statusFilter.isBlank() && !"TODAS".equalsIgnoreCase(statusFilter)) {
            LocalDateTime now = LocalDateTime.now();
            boolean wantActive = "ACTIVAS".equalsIgnoreCase(statusFilter);
            tasks = tasks.stream().filter(t -> {
                if (t.getDueAt() == null) return wantActive; // no deadline = always active
                return wantActive ? !t.getDueAt().isBefore(now) : t.getDueAt().isBefore(now);
            }).toList();
        }

        if (tasks.isEmpty()) {
            return StudentCourseTaskOverviewResponse.builder()
                    .studentId(studentId)
                    .courseId(courseId)
                    .items(List.of())
                    .build();
        }

        List<Long> taskIds = tasks.stream().map(CourseTask::getId).toList();

        // Batch load formats
        Map<Long, List<String>> formatsMap = formatRepo.findByTaskIdIn(taskIds).stream()
                .collect(Collectors.groupingBy(CourseTaskAllowedFormat::getTaskId,
                        Collectors.mapping(CourseTaskAllowedFormat::getExtension, Collectors.toList())));

        // Batch load groups + find student's group ids (DB-filtered)
        Map<Long, List<CourseTaskGroup>> groupsMap = groupRepo.findByTaskIdIn(taskIds).stream()
                .collect(Collectors.groupingBy(CourseTaskGroup::getTaskId));
        List<Long> allGroupIds = groupsMap.values().stream()
                .flatMap(List::stream).map(CourseTaskGroup::getId).toList();
        Set<Long> myGroupIds = allGroupIds.isEmpty() ? Set.of()
                : memberRepo.findByGroupIdInAndStudentId(allGroupIds, studentId).stream()
                        .map(CourseTaskGroupMember::getGroupId)
                        .collect(Collectors.toSet());

        // Batch load submissions for this student (DB-filtered)
        List<CourseTaskSubmission> allSubs = new ArrayList<>(
                submissionRepo.findByTaskIdInAndStudentId(taskIds, studentId));
        if (!myGroupIds.isEmpty()) {
            allSubs.addAll(submissionRepo.findByTaskIdInAndGroupIdIn(taskIds, new ArrayList<>(myGroupIds)));
        }

        // Group submissions by taskId → list (ordered by attemptNumber desc to get latest)
        Map<Long, List<CourseTaskSubmission>> subsByTask = allSubs.stream()
                .collect(Collectors.groupingBy(CourseTaskSubmission::getTaskId));

        List<StudentCourseTaskOverviewResponse.TaskOverviewItem> items = tasks.stream().map(t -> {
            List<CourseTaskSubmission> taskSubs = subsByTask.getOrDefault(t.getId(), List.of());
            int attemptsUsed = taskSubs.size();

            // Latest submission = highest attemptNumber
            CourseTaskSubmission latest = taskSubs.stream()
                    .max(Comparator.comparingInt(CourseTaskSubmission::getAttemptNumber))
                    .orElse(null);

            String pendingReason = (latest == null) ? "NO_SUBMITTED" : "";

            StudentCourseTaskOverviewResponse.LatestSubmissionSummary latestSummary = null;
            if (latest != null) {
                latestSummary = StudentCourseTaskOverviewResponse.LatestSubmissionSummary.builder()
                        .id(latest.getId())
                        .taskId(latest.getTaskId())
                        .courseId(latest.getCourseId())
                        .attemptNumber(latest.getAttemptNumber())
                        .studentId(latest.getStudentId())
                        .studentName(latest.getStudentName())
                        .submittedAt(latest.getSubmittedAt())
                        .status(latest.getStatus())
                        .score(latest.getScore())
                        .feedback(latest.getFeedback())
                        .build();
            }

            StudentCourseTaskOverviewResponse.TaskSummary taskSummary =
                    StudentCourseTaskOverviewResponse.TaskSummary.builder()
                            .id(t.getId())
                            .courseId(t.getCourseId())
                            .title(t.getTitle())
                            .description(t.getDescription())
                            .category(t.getCategory())
                            .visibleFrom(t.getVisibleFrom())
                            .dueAt(t.getDueAt())
                            .status(t.getStatus())
                            .maxScore(t.getMaxScore())
                            .maxAttempts(t.getMaxAttempts())
                            .allowLateSubmission(t.isAllowLateSubmission())
                            .groupTask(t.isGroupTask())
                            .acceptedFormats(formatsMap.getOrDefault(t.getId(), List.of()))
                            .periodId(t.getPeriodId())
                            .periodName(t.getPeriodName())
                            .build();

            return StudentCourseTaskOverviewResponse.TaskOverviewItem.builder()
                    .task(taskSummary)
                    .attemptsUsed(attemptsUsed)
                    .pendingReason(pendingReason)
                    .latestSubmission(latestSummary)
                    .build();
        }).toList();

        return StudentCourseTaskOverviewResponse.builder()
                .studentId(studentId)
                .courseId(courseId)
                .items(items)
                .build();
    }

    public Map<String, Object> getSubmissionSummary(Long institutionId, String taskRef) {
        CourseTask task = resolveTask(taskRef, institutionId);
        List<CourseTaskSubmission> submissions = submissionRepo.findByTaskId(task.getId());
        long total = submissions.size();
        long enviado  = submissions.stream().filter(s -> "ENVIADO".equals(s.getStatus())).count();
        long revisado = submissions.stream().filter(s -> "REVISADO".equals(s.getStatus())).count();
        long tardio   = submissions.stream().filter(s -> "TARDIO".equals(s.getStatus())).count();
        long pendiente = submissions.stream().filter(s -> "PENDIENTE".equals(s.getStatus())).count();
        Map<String, Object> summary = new java.util.LinkedHashMap<>();
        summary.put("taskId", task.getId());
        summary.put("generatedCode", task.getGeneratedCode());
        summary.put("title", task.getTitle());
        summary.put("total", total);
        summary.put("enviado", enviado);
        summary.put("revisado", revisado);
        summary.put("tardio", tardio);
        summary.put("pendiente", pendiente);
        return summary;
    }

    private void saveGroups(Long taskId, List<backend_instituciones.backend_instituciones.dto.request.TaskGroupRequest> groups) {
        if (groups == null) return;
        Set<Long> seenStudents = new HashSet<>();
        for (var gReq : groups) {
            CourseTaskGroup group = groupRepo.save(CourseTaskGroup.builder()
                    .taskId(taskId)
                    .name(gReq.getName())
                    .build());
            if (gReq.getStudentIds() != null) {
                for (Long sid : gReq.getStudentIds()) {
                    if (!seenStudents.add(sid)) {
                        throw new BusinessException(
                                "El alumno " + sid + " está en más de un grupo de la misma tarea",
                                HttpStatus.BAD_REQUEST, "ALUMNO_EN_MULTIPLES_GRUPOS");
                    }
                    memberRepo.save(CourseTaskGroupMember.builder()
                            .groupId(group.getId())
                            .studentId(sid)
                            .build());
                }
            }
        }
    }
}
