package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.*;
import backend_instituciones.backend_instituciones.dto.request.CourseAssignmentRequest;
import backend_instituciones.backend_instituciones.dto.response.CourseAssignmentResponse;
import backend_instituciones.backend_instituciones.dto.response.PageResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseAssignmentService {

    private final CourseAssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final AcademicLevelRepository levelRepository;
    private final AcademicGradeRepository gradeRepository;
    private final AcademicSectionRepository sectionRepository;
    private final AcademicYearRepository academicYearRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final StudentSectionAssignmentRepository studentSectionAssignmentRepository;
    private final SectionScheduleSlotRepository slotRepository;

    public PageResponse<CourseAssignmentResponse> list(Long institutionId, Long classroomId,
                                                        String academicYear, Long levelId, Long gradeId, Long sectionId,
                                                        Long teacherId,
                                                        int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        List<CourseAssignment> assignments;

        if (teacherId != null) {
            assignments = assignmentRepository.findByTeacherIdAndInstitutionId(teacherId, institutionId);
        } else if (classroomId != null) {
            assignments = assignmentRepository.findByClassroomIdAndInstitutionId(classroomId, institutionId);
        } else if (academicYear != null || levelId != null || gradeId != null || sectionId != null) {
            List<Classroom> classrooms = classroomRepository.findByInstitutionId(institutionId);
            List<Long> matchingIds = classrooms.stream()
                    .filter(c -> academicYear == null || academicYear.equals(c.getAcademicYear()))
                    .filter(c -> levelId == null || levelId.equals(c.getAcademicLevelId()))
                    .filter(c -> gradeId == null || gradeId.equals(c.getAcademicGradeId()))
                    .filter(c -> sectionId == null || sectionId.equals(c.getAcademicSectionId()))
                    .map(Classroom::getId)
                    .toList();
            assignments = matchingIds.isEmpty()
                    ? List.of()
                    : assignmentRepository.findByClassroomIdInAndInstitutionId(matchingIds, institutionId);
        } else {
            var pageResult = assignmentRepository.findByInstitutionId(institutionId, pageable);
            List<CourseAssignmentResponse> mapped = toBatchResponses(pageResult.getContent());
            return PageResponse.from(new PageImpl<>(mapped, pageable, pageResult.getTotalElements()));
        }

        List<CourseAssignmentResponse> mapped = toBatchResponses(assignments);
        return PageResponse.from(new PageImpl<>(mapped, pageable, assignments.size()));
    }

    public List<CourseAssignmentResponse> listByClassroom(Long classroomId, Long institutionId) {
        List<CourseAssignment> assignments = assignmentRepository.findByClassroomIdAndInstitutionId(classroomId, institutionId);
        return toBatchResponses(assignments);
    }

    public CourseAssignmentResponse get(Long id, Long institutionId) {
        CourseAssignment a = assignmentRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseAssignment", id));
        return toBatchResponses(List.of(a)).get(0);
    }

    public List<Map<String, Object>> getStudents(Long assignmentId, Long institutionId) {
        CourseAssignment assignment = assignmentRepository.findByIdAndInstitutionId(assignmentId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseAssignment", assignmentId));

        // Classroom holds level/grade/section ids — use StudentSectionAssignment as source of truth
        Classroom classroom = classroomRepository.findById(assignment.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", assignment.getClassroomId()));

        List<StudentSectionAssignment> sectionAssignments =
                studentSectionAssignmentRepository.findByInstitutionIdAndLevelIdAndGradeIdAndSectionId(
                        institutionId,
                        classroom.getAcademicLevelId(),
                        classroom.getAcademicGradeId(),
                        classroom.getAcademicSectionId());

        if (sectionAssignments.isEmpty()) return List.of();

        Set<Long> studentIds = sectionAssignments.stream()
                .map(StudentSectionAssignment::getStudentId)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return sectionAssignments.stream().map((StudentSectionAssignment sa) -> {
            User u = userMap.get(sa.getStudentId());
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("studentId", sa.getStudentId());
            result.put("name", u != null ? u.getName() : null);
            result.put("documentNumber", u != null ? u.getDocumentNumber() : null);
            result.put("email", u != null ? u.getEmail() : null);
            result.put("assignedAt", sa.getCreatedAt());
            return result;
        }).toList();
    }

    @Transactional
    public CourseAssignmentResponse create(Long institutionId, CourseAssignmentRequest request) {
        AcademicYear year = academicYearRepository.findByIdAndInstitutionId(request.getAcademicYearId(), institutionId)
                .orElseThrow(() -> new BusinessException("Academic year not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));

        AcademicLevel level = levelRepository.findByIdAndInstitutionId(request.getLevelId(), institutionId)
                .orElseThrow(() -> new BusinessException("Academic level not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));

        AcademicGrade grade = gradeRepository.findByIdAndLevelId(request.getGradeId(), level.getId())
                .orElseThrow(() -> new BusinessException("Academic grade not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));

        AcademicSection section = sectionRepository.findByIdAndGradeId(request.getSectionId(), grade.getId())
                .orElseThrow(() -> new BusinessException("Academic section not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));

        Classroom classroom = findOrCreateClassroom(institutionId, year, level, grade, section);

        Course course = courseRepository.findByIdAndInstitutionId(request.getCourseCatalogId(), institutionId)
                .orElseThrow(() -> new BusinessException("Course not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));

        if (assignmentRepository.existsByClassroomIdAndCourseId(classroom.getId(), course.getId())) {
            throw new BusinessException("Course already assigned to this classroom", HttpStatus.CONFLICT, "ALREADY_ASSIGNED");
        }

        String code = generateCode(level, grade, section, course.getName(), year.getName());
        String displayName = classroom.getDisplayName() != null ? classroom.getDisplayName() : classroom.getName();

        User teacher = userRepository.findById(request.getTeacherId())
                .filter(u -> u.getInstitutionId().equals(institutionId))
                .orElseThrow(() -> new BusinessException("Teacher not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));

        CourseAssignment assignment = CourseAssignment.builder()
                .institutionId(institutionId)
                .classroomId(classroom.getId())
                .courseId(course.getId())
                .courseName(course.getName())
                .classroomName(displayName)
                .academicYear(year.getName())
                .levelId(level.getId())
                .gradeName(grade.getName())
                .gradeId(grade.getId())
                .sectionName(section.getName())
                .sectionId(section.getId())
                .levelName(level.getName())
                .teacherId(request.getTeacherId())
                .teacherName(teacher.getName())
                .generatedCode(code)
                .status("ACTIVE")
                .build();

        return toBatchResponses(List.of(assignmentRepository.save(assignment))).get(0);
    }

    @Transactional
    public CourseAssignmentResponse update(Long id, Long institutionId, CourseAssignmentRequest request) {
        CourseAssignment assignment = assignmentRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseAssignment", id));
        User teacher = userRepository.findById(request.getTeacherId())
                .filter(u -> u.getInstitutionId().equals(institutionId))
                .orElseThrow(() -> new BusinessException("Teacher not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        assignment.setTeacherId(request.getTeacherId());
        assignment.setTeacherName(teacher.getName());
        CourseAssignment saved = assignmentRepository.save(assignment);

        // Sync schedule slots — slots store teacherId independently, must stay in sync
        slotRepository.updateTeacherIdByCourseId(saved.getId(), request.getTeacherId());

        return toBatchResponses(List.of(saved)).get(0);
    }

    @Transactional
    public void delete(Long id, Long institutionId) {
        CourseAssignment a = assignmentRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseAssignment", id));
        assignmentRepository.delete(a);
    }

    // ── batch response builder (1 query per entity type, not per row) ──────────

    public List<CourseAssignmentResponse> toBatchResponsesPublic(List<CourseAssignment> assignments) {
        return toBatchResponses(assignments);
    }

    private List<CourseAssignmentResponse> toBatchResponses(List<CourseAssignment> assignments) {
        if (assignments.isEmpty()) return List.of();

        // Denormalized fields stored on entity — no extra queries needed.
        // Fallback: for legacy rows created before denormalization, fetch classroom + lookup names.
        Set<Long> legacyIds = assignments.stream()
                .filter(a -> a.getLevelName() == null && a.getClassroomId() != null)
                .map(CourseAssignment::getClassroomId)
                .collect(Collectors.toSet());

        Map<Long, Classroom> classroomFallback = legacyIds.isEmpty()
                ? Map.of()
                : classroomRepository.findAllById(legacyIds).stream()
                        .collect(Collectors.toMap(Classroom::getId, Function.identity()));

        // Resolve names for legacy rows only (one-time cost, disappears after all rows backfilled)
        Set<Long> fallbackLevelIds = classroomFallback.values().stream()
                .map(Classroom::getAcademicLevelId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> fallbackGradeIds = classroomFallback.values().stream()
                .map(Classroom::getAcademicGradeId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> fallbackSectionIds = classroomFallback.values().stream()
                .map(Classroom::getAcademicSectionId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> fallbackLevelNames = fallbackLevelIds.isEmpty() ? Map.of()
                : levelRepository.findAllById(fallbackLevelIds).stream()
                        .collect(Collectors.toMap(AcademicLevel::getId, AcademicLevel::getName));
        Map<Long, String> fallbackGradeNames = fallbackGradeIds.isEmpty() ? Map.of()
                : gradeRepository.findAllById(fallbackGradeIds).stream()
                        .collect(Collectors.toMap(AcademicGrade::getId, AcademicGrade::getName));
        Map<Long, String> fallbackSectionNames = fallbackSectionIds.isEmpty() ? Map.of()
                : sectionRepository.findAllById(fallbackSectionIds).stream()
                        .collect(Collectors.toMap(AcademicSection::getId, AcademicSection::getName));

        // Teacher name fallback for legacy rows
        Set<Long> legacyTeacherIds = assignments.stream()
                .filter(a -> a.getTeacherName() == null)
                .map(CourseAssignment::getTeacherId)
                .collect(Collectors.toSet());
        Map<Long, String> fallbackTeacherNames = legacyTeacherIds.isEmpty() ? Map.of()
                : userRepository.findAllById(legacyTeacherIds).stream()
                        .collect(Collectors.toMap(User::getId, User::getName));

        return assignments.stream().map(a -> {
            // Prefer denormalized fields; fall back to live-lookup for legacy rows
            String classroomName = a.getClassroomName();
            String levelName = a.getLevelName();
            String gradeName = a.getGradeName();
            String sectionName = a.getSectionName();
            String academicYear = a.getAcademicYear();
            Long levelId = a.getLevelId();
            Long gradeId = a.getGradeId();
            Long sectionId = a.getSectionId();
            String teacherName = a.getTeacherName() != null
                    ? a.getTeacherName()
                    : fallbackTeacherNames.get(a.getTeacherId());

            if (levelName == null && classroomFallback.containsKey(a.getClassroomId())) {
                Classroom c = classroomFallback.get(a.getClassroomId());
                if (classroomName == null)
                    classroomName = c.getDisplayName() != null ? c.getDisplayName() : c.getName();
                if (academicYear == null) academicYear = c.getAcademicYear();
                levelId = c.getAcademicLevelId();
                gradeId = c.getAcademicGradeId();
                sectionId = c.getAcademicSectionId();
                levelName = levelId != null ? fallbackLevelNames.get(levelId) : null;
                gradeName = gradeId != null ? fallbackGradeNames.get(gradeId) : null;
                sectionName = sectionId != null ? fallbackSectionNames.get(sectionId) : null;
            }

            return CourseAssignmentResponse.builder()
                    .id(a.getId())
                    .institutionId(a.getInstitutionId())
                    .generatedCode(a.getGeneratedCode())
                    .status(a.getStatus())
                    .classroomId(a.getClassroomId())
                    .classroomName(classroomName)
                    .levelId(levelId)
                    .gradeId(gradeId)
                    .sectionId(sectionId)
                    .educationLevel(levelName)
                    .grade(gradeName)
                    .section(sectionName)
                    .academicYear(academicYear)
                    .courseCatalogId(a.getCourseId())
                    .courseName(a.getCourseName())
                    .courseArea(null)
                    .teacherUserId(a.getTeacherId())
                    .teacherName(teacherName)
                    .createdAt(a.getCreatedAt())
                    .build();
        }).toList();
    }

    private Classroom findOrCreateClassroom(Long institutionId, AcademicYear year,
                                            AcademicLevel level, AcademicGrade grade, AcademicSection section) {
        return classroomRepository
                .findByInstitutionIdAndAcademicYearIdAndAcademicLevelIdAndAcademicGradeIdAndAcademicSectionId(
                        institutionId, year.getId(), level.getId(), grade.getId(), section.getId())
                .orElseGet(() -> {
                    String displayName = level.getName() + " - " + grade.getName() + " - " + section.getName() + " - " + year.getName();
                    return classroomRepository.save(Classroom.builder()
                            .institutionId(institutionId)
                            .name(displayName)
                            .displayName(displayName)
                            .academicLevelId(level.getId())
                            .academicGradeId(grade.getId())
                            .academicSectionId(section.getId())
                            .academicYearId(year.getId())
                            .academicYear(year.getName())
                            .build());
                });
    }

    private String generateCode(AcademicLevel level, AcademicGrade grade, AcademicSection section,
                                 String courseName, String yearName) {
        String levelCode = level.getCode();
        String gradeCode = grade.getName().replace(" AÑOS", "A").replace(" ", "").toUpperCase();
        String courseCode = courseName.replaceAll("[^A-Za-z]", "").toUpperCase();
        courseCode = courseCode.length() >= 3 ? courseCode.substring(0, 3) : courseCode;
        String sectionCode = section.getName().toUpperCase();

        String base = levelCode + "-" + gradeCode + "-" + sectionCode + "-" + courseCode + "-" + yearName;

        if (!assignmentRepository.existsByGeneratedCode(base)) return base;

        int suffix = 2;
        while (assignmentRepository.existsByGeneratedCode(base + "-" + suffix)) suffix++;
        return base + "-" + suffix;
    }
}
