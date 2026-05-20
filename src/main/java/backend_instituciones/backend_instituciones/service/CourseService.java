package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.Course;
import backend_instituciones.backend_instituciones.domain.entity.CourseMaterial;
import backend_instituciones.backend_instituciones.domain.entity.Enrollment;
import backend_instituciones.backend_instituciones.dto.request.CreateCourseRequest;
import backend_instituciones.backend_instituciones.dto.request.EnrollRequest;
import backend_instituciones.backend_instituciones.dto.response.CourseResponse;
import backend_instituciones.backend_instituciones.dto.response.PageResponse;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.CourseMaterialRepository;
import backend_instituciones.backend_instituciones.repository.CourseAssignmentRepository;
import backend_instituciones.backend_instituciones.repository.CourseRepository;
import backend_instituciones.backend_instituciones.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseMaterialRepository materialRepository;

    public PageResponse<CourseResponse> list(Long institutionId, int page, int size) {
        return PageResponse.from(
                courseRepository.findByInstitutionId(institutionId, PageRequest.of(page, size))
                        .map(this::toResponse));
    }

    public List<CourseResponse> listAll(Long institutionId) {
        return courseRepository.findByInstitutionId(institutionId, PageRequest.of(0, 500))
                .map(this::toResponse).toList();
    }

    public CourseResponse get(Long id, Long institutionId) {
        return courseRepository.findByIdAndInstitutionId(id, institutionId)
                .map(this::toResponse)
                .orElseGet(() -> courseAssignmentRepository.findByIdAndInstitutionId(id, institutionId)
                        .map(a -> CourseResponse.builder()
                                .id(a.getId())
                                .institutionId(institutionId)
                                .name(a.getCourseName())
                                .status(a.getStatus())
                                .createdAt(a.getCreatedAt())
                                .build())
                        .orElseThrow(() -> new ResourceNotFoundException("Course", id)));
    }

    @Transactional
    public CourseResponse create(Long institutionId, CreateCourseRequest request) {
        Course course = Course.builder()
                .institutionId(institutionId)
                .name(request.getName())
                .description(request.getDescription())
                .area(request.getArea())
                .status("ACTIVE")
                .build();
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse update(Long id, Long institutionId, CreateCourseRequest request) {
        Course course = findOrThrow(id, institutionId);
        course.setName(request.getName());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getArea() != null) course.setArea(request.getArea());
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public void delete(Long id, Long institutionId) {
        courseRepository.delete(findOrThrow(id, institutionId));
    }

    @Transactional
    public void enroll(Long courseId, Long institutionId, EnrollRequest request) {
        findOrThrow(courseId, institutionId);
        for (Long studentId : request.getStudentIds()) {
            if (!enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
                enrollmentRepository.save(Enrollment.builder()
                        .courseId(courseId)
                        .studentId(studentId)
                        .build());
            }
        }
    }

    @Transactional
    public void unenroll(Long courseId, Long institutionId, Long studentId) {
        findOrThrow(courseId, institutionId);
        enrollmentRepository.deleteByCourseIdAndStudentId(courseId, studentId);
    }

    public List<Long> getStudents(Long courseId, Long institutionId) {
        validateCourseOrAssignment(courseId, institutionId);
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(Enrollment::getStudentId).toList();
    }

    public List<CourseMaterial> getMaterials(Long courseId, Long institutionId) {
        validateCourseOrAssignment(courseId, institutionId);
        return materialRepository.findByCourseId(courseId);
    }

    @Transactional
    public CourseMaterial addMaterial(Long courseId, Long institutionId, Long uploadedBy, String title, String fileUrl) {
        findOrThrow(courseId, institutionId);
        return materialRepository.save(CourseMaterial.builder()
                .courseId(courseId)
                .title(title)
                .fileUrl(fileUrl)
                .uploadedBy(uploadedBy)
                .build());
    }

    @Transactional
    public void deleteMaterial(Long courseId, Long institutionId, Long materialId) {
        findOrThrow(courseId, institutionId);
        materialRepository.deleteById(materialId);
    }

    public Course findOrThrow(Long id, Long institutionId) {
        return courseRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    /** Validates that id belongs to either courses or course_assignments for this institution. */
    private void validateCourseOrAssignment(Long id, Long institutionId) {
        boolean exists = courseRepository.findByIdAndInstitutionId(id, institutionId).isPresent()
                || courseAssignmentRepository.findByIdAndInstitutionId(id, institutionId).isPresent();
        if (!exists) throw new ResourceNotFoundException("Course", id);
    }

    public CourseResponse toResponse(Course c) {
        return CourseResponse.builder()
                .id(c.getId())
                .institutionId(c.getInstitutionId())
                .name(c.getName())
                .description(c.getDescription())
                .area(c.getArea())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
