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

import java.util.List;

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

    public PageResponse<CourseAssignmentResponse> list(Long institutionId, Long classroomId,
                                                        String academicYear, Long levelId, Long gradeId, Long sectionId,
                                                        int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (classroomId != null) {
            List<CourseAssignment> filtered = assignmentRepository.findByClassroomIdAndInstitutionId(classroomId, institutionId);
            List<CourseAssignmentResponse> mapped = filtered.stream().map(a -> toResponse(a, institutionId)).toList();
            return PageResponse.from(new PageImpl<>(mapped, pageable, filtered.size()));
        }

        if (academicYear != null || levelId != null || gradeId != null || sectionId != null) {
            List<Classroom> classrooms = classroomRepository.findByInstitutionId(institutionId);
            List<Long> matchingIds = classrooms.stream()
                    .filter(c -> academicYear == null || academicYear.equals(c.getAcademicYear()))
                    .filter(c -> levelId == null || levelId.equals(c.getAcademicLevelId()))
                    .filter(c -> gradeId == null || gradeId.equals(c.getAcademicGradeId()))
                    .filter(c -> sectionId == null || sectionId.equals(c.getAcademicSectionId()))
                    .map(Classroom::getId)
                    .toList();
            List<CourseAssignment> filtered = matchingIds.isEmpty()
                    ? List.of()
                    : assignmentRepository.findByClassroomIdInAndInstitutionId(matchingIds, institutionId);
            List<CourseAssignmentResponse> mapped = filtered.stream().map(a -> toResponse(a, institutionId)).toList();
            return PageResponse.from(new PageImpl<>(mapped, pageable, filtered.size()));
        }

        return PageResponse.from(assignmentRepository.findByInstitutionId(institutionId, pageable)
                .map(a -> toResponse(a, institutionId)));
    }

    public List<CourseAssignmentResponse> listByClassroom(Long classroomId, Long institutionId) {
        return assignmentRepository.findByClassroomIdAndInstitutionId(classroomId, institutionId)
                .stream().map(a -> toResponse(a, institutionId)).toList();
    }

    public CourseAssignmentResponse get(Long id, Long institutionId) {
        CourseAssignment a = assignmentRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseAssignment", id));
        return toResponse(a, institutionId);
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

        userRepository.findById(request.getTeacherId())
                .filter(u -> u.getInstitutionId().equals(institutionId))
                .orElseThrow(() -> new BusinessException("Teacher not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));

        if (assignmentRepository.existsByClassroomIdAndCourseId(classroom.getId(), course.getId())) {
            throw new BusinessException("Course already assigned to this classroom", HttpStatus.CONFLICT, "ALREADY_ASSIGNED");
        }

        String code = generateCode(level, grade, section, course.getName(), year.getName());

        CourseAssignment assignment = CourseAssignment.builder()
                .institutionId(institutionId)
                .classroomId(classroom.getId())
                .courseId(course.getId())
                .teacherId(request.getTeacherId())
                .generatedCode(code)
                .status("ACTIVE")
                .build();

        return toResponse(assignmentRepository.save(assignment), institutionId);
    }

    @Transactional
    public CourseAssignmentResponse update(Long id, Long institutionId, CourseAssignmentRequest request) {
        CourseAssignment assignment = assignmentRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseAssignment", id));
        userRepository.findById(request.getTeacherId())
                .filter(u -> u.getInstitutionId().equals(institutionId))
                .orElseThrow(() -> new BusinessException("Teacher not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        assignment.setTeacherId(request.getTeacherId());
        return toResponse(assignmentRepository.save(assignment), institutionId);
    }

    @Transactional
    public void delete(Long id, Long institutionId) {
        CourseAssignment a = assignmentRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseAssignment", id));
        assignmentRepository.delete(a);
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

    CourseAssignmentResponse toResponse(CourseAssignment a, Long institutionId) {
        Classroom classroom = classroomRepository.findById(a.getClassroomId()).orElse(null);
        String classroomName = classroom != null ? classroom.getDisplayName() : null;
        if (classroomName == null && classroom != null) classroomName = classroom.getName();

        String educationLevel = null, gradeName = null, sectionName = null, academicYear = null;

        if (classroom != null) {
            academicYear = classroom.getAcademicYear();
            if (classroom.getAcademicLevelId() != null) {
                educationLevel = levelRepository.findById(classroom.getAcademicLevelId())
                        .map(AcademicLevel::getName).orElse(null);
            }
            if (classroom.getAcademicGradeId() != null) {
                gradeName = gradeRepository.findById(classroom.getAcademicGradeId())
                        .map(AcademicGrade::getName).orElse(null);
            }
            if (classroom.getAcademicSectionId() != null) {
                sectionName = sectionRepository.findById(classroom.getAcademicSectionId())
                        .map(AcademicSection::getName).orElse(null);
            }
        }

        Course course = courseRepository.findById(a.getCourseId()).orElse(null);
        String teacherName = userRepository.findById(a.getTeacherId()).map(User::getName).orElse(null);

        return CourseAssignmentResponse.builder()
                .id(a.getId())
                .institutionId(a.getInstitutionId())
                .generatedCode(a.getGeneratedCode())
                .status(a.getStatus())
                .classroomId(a.getClassroomId())
                .classroomName(classroomName)
                .educationLevel(educationLevel)
                .grade(gradeName)
                .section(sectionName)
                .academicYear(academicYear)
                .courseCatalogId(a.getCourseId())
                .courseName(course != null ? course.getName() : null)
                .courseArea(course != null ? course.getArea() : null)
                .teacherUserId(a.getTeacherId())
                .teacherName(teacherName)
                .createdAt(a.getCreatedAt())
                .build();
    }
}
