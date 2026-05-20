package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.*;
import backend_instituciones.backend_instituciones.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves students enrolled in a course and pushes SSE events to them.
 * assignmentId = CourseAssignment PK = CourseTask.courseId / CourseContentPost.courseId
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseNotificationService {

    private final CourseAssignmentRepository courseAssignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final StudentSectionAssignmentRepository studentSectionAssignmentRepository;
    private final UserRepository userRepository;
    private final SseService sseService;

    // ── Nueva tarea ──────────────────────────────────────────────────────────────

    @Async("taskExecutor")
    public void notifyStudentsNewTask(Long institutionId, Long assignmentId,
                                      Long taskId, String title, String descripcion,
                                      LocalDateTime dueAt, Long periodId, String periodName) {
        CourseContext ctx = resolveContext(institutionId, assignmentId);
        if (ctx == null) return;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", taskId);
        payload.put("tipo", "TAREA");
        payload.put("courseId", assignmentId);
        payload.put("cursoNombre", ctx.courseName);
        payload.put("title", title);
        payload.put("descripcion", descripcion);
        payload.put("dueAt", dueAt);
        payload.put("periodId", periodId);
        payload.put("periodName", periodName);
        payload.put("teacherId", ctx.teacherId);
        payload.put("teacherName", ctx.teacherName);
        payload.put("targetRole", "ESTUDIANTE");

        sendToStudents(ctx, "new_task", payload);
    }

    // ── Nuevo contenido ──────────────────────────────────────────────────────────

    @Async("taskExecutor")
    public void notifyStudentsNewContent(Long institutionId, Long assignmentId,
                                         Long contentId, String title, String descripcion,
                                         Long periodId, String periodName) {
        CourseContext ctx = resolveContext(institutionId, assignmentId);
        if (ctx == null) return;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", contentId);
        payload.put("tipo", "CONTENIDO");
        payload.put("courseId", assignmentId);
        payload.put("cursoNombre", ctx.courseName);
        payload.put("title", title);
        payload.put("resumen", descripcion);
        payload.put("descripcion", descripcion);
        payload.put("periodId", periodId);
        payload.put("periodName", periodName);
        payload.put("teacherId", ctx.teacherId);
        payload.put("teacherName", ctx.teacherName);
        payload.put("targetRole", "ESTUDIANTE");

        sendToStudents(ctx, "new_content", payload);
    }

    // ── Nueva evaluación ─────────────────────────────────────────────────────────

    @Async("taskExecutor")
    public void notifyStudentsNewAssessment(Long institutionId, Long assignmentId,
                                            Long assessmentId, String title, String descripcion,
                                            Long periodId, String periodName) {
        CourseContext ctx = resolveContext(institutionId, assignmentId);
        if (ctx == null) return;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", assessmentId);
        payload.put("tipo", "EVALUACION");
        payload.put("courseId", assignmentId);
        payload.put("cursoNombre", ctx.courseName);
        payload.put("title", title);
        payload.put("descripcion", descripcion);
        payload.put("periodId", periodId);
        payload.put("periodName", periodName);
        payload.put("teacherId", ctx.teacherId);
        payload.put("teacherName", ctx.teacherName);
        payload.put("targetRole", "ESTUDIANTE");

        sendToStudents(ctx, "new_assessment", payload);
    }

    // ── Entrega revisada ─────────────────────────────────────────────────────────

    @Async("taskExecutor")
    public void notifySubmissionReviewed(Long studentId, Long submissionId,
                                          Long taskId, Long assignmentId, Long institutionId,
                                          BigDecimal score, String feedback, Long teacherId) {
        CourseContext ctx = resolveContext(institutionId, assignmentId);
        String cursoNombre = ctx != null ? ctx.courseName : null;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("submissionId", submissionId);
        payload.put("taskId", taskId);
        payload.put("courseId", assignmentId);
        payload.put("cursoNombre", cursoNombre);
        payload.put("score", score);
        payload.put("status", "REVISADO");
        payload.put("feedback", feedback);
        payload.put("studentId", studentId);
        payload.put("teacherId", teacherId);

        try {
            sseService.sendToUser(studentId.toString(), "submission_reviewed", payload);
            log.debug("SSE submission_reviewed → studentId={}", studentId);
        } catch (Exception e) {
            log.warn("SSE submission_reviewed error studentId={}: {}", studentId, e.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private void sendToStudents(CourseContext ctx, String eventName, Object payload) {
        log.debug("SSE {}: assignmentId={} students={}", eventName, ctx.assignmentId, ctx.students.size());
        for (StudentSectionAssignment ssa : ctx.students) {
            sseService.sendToUser(ssa.getStudentId().toString(), eventName, payload);
            log.debug("SSE {} → studentId={}", eventName, ssa.getStudentId());
        }
    }

    private CourseContext resolveContext(Long institutionId, Long assignmentId) {
        try {
            CourseAssignment assignment = courseAssignmentRepository
                    .findByIdAndInstitutionId(assignmentId, institutionId)
                    .orElse(null);
            if (assignment == null) {
                log.warn("SSE: no assignment id={} institution={}", assignmentId, institutionId);
                return null;
            }

            Classroom classroom = classroomRepository.findById(assignment.getClassroomId()).orElse(null);
            if (classroom == null) {
                log.warn("SSE: no classroom id={}", assignment.getClassroomId());
                return null;
            }

            List<StudentSectionAssignment> students =
                    studentSectionAssignmentRepository.findByInstitutionIdAndLevelIdAndGradeIdAndSectionId(
                            institutionId,
                            classroom.getAcademicLevelId(),
                            classroom.getAcademicGradeId(),
                            classroom.getAcademicSectionId());

            String teacherName = userRepository.findById(assignment.getTeacherId())
                    .map(User::getName).orElse(null);

            return new CourseContext(assignmentId, assignment.getCourseName(),
                    assignment.getTeacherId(), teacherName, students);
        } catch (Exception e) {
            log.warn("SSE resolveContext error assignmentId={}: {}", assignmentId, e.getMessage(), e);
            return null;
        }
    }

    private record CourseContext(
            Long assignmentId,
            String courseName,
            Long teacherId,
            String teacherName,
            List<StudentSectionAssignment> students
    ) {}
}
