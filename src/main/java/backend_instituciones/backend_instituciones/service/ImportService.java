package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.*;
import backend_instituciones.backend_instituciones.domain.enums.Role;
import backend_instituciones.backend_instituciones.dto.response.ImportJobResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String[] STUDENT_HEADERS = {
            "studentCode", "documentType", "documentNumber", "firstName", "lastName",
            "birthDate", "gender", "email", "phone", "address", "district", "city",
            "gradeLevel", "section", "academicYear", "bloodType", "allergies", "medicalNotes",
            "fatherName", "fatherDocument", "fatherEmail", "fatherPhone",
            "motherName", "motherDocument", "motherEmail", "motherPhone",
            "guardianName", "guardianDocument", "guardianEmail", "guardianPhone"
    };

    private static final String[] TEACHER_HEADERS = {
            "employeeCode", "documentType", "documentNumber", "firstName", "lastName",
            "email", "phone", "address", "specialty", "hireDate"
    };

    private static final String[] GUARDIAN_HEADERS = {
            "documentType", "documentNumber", "firstName", "lastName", "email", "phone",
            "address", "occupation", "workplace", "studentDocumentNumber", "relationship"
    };

    private final ImportJobRepository importJobRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final StudentGuardianRepository guardianRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final GuardianProfileRepository guardianProfileRepository;

    public ImportJobResponse getJob(String jobId, Long institutionId) {
        ImportJob job = importJobRepository.findByIdAndInstitutionId(jobId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("ImportJob", jobId));
        return toResponse(job);
    }

    @Transactional
    public ImportJobResponse importStudents(Long institutionId, Long classroomId, String academicYear,
                                            MultipartFile file) {
        if (classroomId != null) {
            classroomRepository.findByIdAndInstitutionId(classroomId, institutionId)
                    .orElseThrow(() -> new BusinessException("Classroom not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        }

        String jobId = "imp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        ImportJob job = ImportJob.builder()
                .id(jobId).institutionId(institutionId).type("STUDENT").status("PROCESSING")
                .classroomId(classroomId).academicYear(academicYear).build();
        importJobRepository.save(job);

        List<Map<String, Object>> errors = new ArrayList<>();
        int processed = 0, created = 0, updated = 0, failed = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new BusinessException("Empty file", HttpStatus.BAD_REQUEST, "EMPTY_FILE");
            Map<String, Integer> colIndex = buildColumnIndex(headerRow);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;
                processed++;
                try {
                    String email = getCellString(row, colIndex, "email");
                    String firstName = getCellString(row, colIndex, "firstName");
                    String lastName = getCellString(row, colIndex, "lastName");
                    String documentNumber = getCellString(row, colIndex, "documentNumber");

                    if (email == null || email.isBlank()) throw new IllegalArgumentException("email is required");
                    if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("firstName is required");

                    String name = (firstName + " " + (lastName != null ? lastName : "")).trim();
                    Optional<User> existing = (documentNumber != null)
                            ? userRepository.findByDocumentNumberAndInstitutionId(documentNumber, institutionId)
                            : userRepository.findByEmailAndInstitutionId(email.toLowerCase(), institutionId);

                    User user;
                    if (existing.isPresent()) {
                        user = existing.get();
                        user.setName(name);
                        user.setFirstName(firstName);
                        user.setLastName(lastName);
                        user.setEmail(email.toLowerCase().trim());
                        applyStudentCommonFields(user, row, colIndex);
                        user = userRepository.save(user);
                        applyStudentProfile(user.getId(), row, colIndex, false);
                        updated++;
                    } else {
                        user = User.builder()
                                .institutionId(institutionId).name(name).firstName(firstName).lastName(lastName)
                                .email(email.toLowerCase().trim())
                                .passwordHash(passwordEncoder.encode(documentNumber != null ? documentNumber : UUID.randomUUID().toString()))
                                .role(Role.ESTUDIANTE).isActive(true).mustCompleteProfile(true).mustChangePassword(true)
                                .build();
                        applyStudentCommonFields(user, row, colIndex);
                        user = userRepository.save(user);
                        applyStudentProfile(user.getId(), row, colIndex, true);
                        created++;
                    }

                    if (classroomId != null && !classroomStudentRepository.existsByClassroomIdAndStudentId(classroomId, user.getId())) {
                        classroomStudentRepository.save(ClassroomStudent.builder()
                                .classroomId(classroomId).studentId(user.getId()).institutionId(institutionId).build());
                    }
                } catch (Exception e) {
                    failed++;
                    Map<String, Object> err = new LinkedHashMap<>();
                    err.put("row", i + 1);
                    err.put("message", e.getMessage());
                    errors.add(err);
                }
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (IOException e) {
            throw new BusinessException("Failed to read file: " + e.getMessage(), HttpStatus.BAD_REQUEST, "FILE_READ_ERROR");
        }

        return finishJob(job, processed, created, updated, failed, errors);
    }

    @Transactional
    public ImportJobResponse importTeachers(Long institutionId, MultipartFile file) {
        String jobId = "imp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        ImportJob job = ImportJob.builder()
                .id(jobId).institutionId(institutionId).type("TEACHER").status("PROCESSING").build();
        importJobRepository.save(job);

        List<Map<String, Object>> errors = new ArrayList<>();
        int processed = 0, created = 0, updated = 0, failed = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new BusinessException("Empty file", HttpStatus.BAD_REQUEST, "EMPTY_FILE");
            Map<String, Integer> colIndex = buildColumnIndex(headerRow);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;
                processed++;
                try {
                    String email = getCellString(row, colIndex, "email");
                    String firstName = getCellString(row, colIndex, "firstName");
                    String lastName = getCellString(row, colIndex, "lastName");
                    String documentNumber = getCellString(row, colIndex, "documentNumber");

                    if (email == null || email.isBlank()) throw new IllegalArgumentException("email is required");

                    String name = (firstName + " " + (lastName != null ? lastName : "")).trim();
                    Optional<User> existing = (documentNumber != null)
                            ? userRepository.findByDocumentNumberAndInstitutionId(documentNumber, institutionId)
                            : userRepository.findByEmailAndInstitutionId(email.toLowerCase(), institutionId);

                    if (existing.isPresent()) {
                        User user = existing.get();
                        user.setName(name);
                        user.setFirstName(firstName);
                        user.setLastName(lastName);
                        user.setPhone(getCellString(row, colIndex, "phone"));
                        user = userRepository.save(user);
                        applyTeacherProfile(user.getId(), row, colIndex, false);
                        updated++;
                    } else {
                        User user = User.builder()
                                .institutionId(institutionId).name(name).firstName(firstName).lastName(lastName)
                                .email(email.toLowerCase().trim())
                                .passwordHash(passwordEncoder.encode(documentNumber != null ? documentNumber : UUID.randomUUID().toString()))
                                .role(Role.DOCENTE).isActive(true).mustCompleteProfile(true).mustChangePassword(true)
                                .documentNumber(documentNumber)
                                .phone(getCellString(row, colIndex, "phone"))
                                .address(getCellString(row, colIndex, "address"))
                                .build();
                        user = userRepository.save(user);
                        applyTeacherProfile(user.getId(), row, colIndex, true);
                        created++;
                    }
                } catch (Exception e) {
                    failed++;
                    Map<String, Object> err = new LinkedHashMap<>();
                    err.put("row", i + 1);
                    err.put("message", e.getMessage());
                    errors.add(err);
                }
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (IOException e) {
            throw new BusinessException("Failed to read file: " + e.getMessage(), HttpStatus.BAD_REQUEST, "FILE_READ_ERROR");
        }

        return finishJob(job, processed, created, updated, failed, errors);
    }

    @Transactional
    public ImportJobResponse importGuardians(Long institutionId, MultipartFile file) {
        String jobId = "imp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        ImportJob job = ImportJob.builder()
                .id(jobId).institutionId(institutionId).type("GUARDIAN").status("PROCESSING").build();
        importJobRepository.save(job);

        List<Map<String, Object>> errors = new ArrayList<>();
        int processed = 0, created = 0, updated = 0, failed = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new BusinessException("Empty file", HttpStatus.BAD_REQUEST, "EMPTY_FILE");
            Map<String, Integer> colIndex = buildColumnIndex(headerRow);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;
                processed++;
                try {
                    String email = getCellString(row, colIndex, "email");
                    String firstName = getCellString(row, colIndex, "firstName");
                    String lastName = getCellString(row, colIndex, "lastName");
                    String documentNumber = getCellString(row, colIndex, "documentNumber");
                    if (email == null || email.isBlank()) throw new IllegalArgumentException("email is required");

                    String name = (firstName + " " + (lastName != null ? lastName : "")).trim();
                    Optional<User> existing = (documentNumber != null)
                            ? userRepository.findByDocumentNumberAndInstitutionId(documentNumber, institutionId)
                            : userRepository.findByEmailAndInstitutionId(email.toLowerCase(), institutionId);

                    User guardian;
                    if (existing.isPresent()) {
                        guardian = existing.get();
                        guardian.setName(name);
                        guardian.setFirstName(firstName);
                        guardian.setLastName(lastName);
                        guardian.setPhone(getCellString(row, colIndex, "phone"));
                        guardian = userRepository.save(guardian);
                        applyGuardianProfile(guardian.getId(), row, colIndex, false);
                        updated++;
                    } else {
                        guardian = User.builder()
                                .institutionId(institutionId).name(name).firstName(firstName).lastName(lastName)
                                .email(email.toLowerCase().trim())
                                .passwordHash(passwordEncoder.encode(documentNumber != null ? documentNumber : UUID.randomUUID().toString()))
                                .role(Role.PADRE).isActive(true).mustCompleteProfile(true).mustChangePassword(true)
                                .documentNumber(documentNumber)
                                .documentType(getCellString(row, colIndex, "documentType"))
                                .phone(getCellString(row, colIndex, "phone"))
                                .address(getCellString(row, colIndex, "address"))
                                .build();
                        guardian = userRepository.save(guardian);
                        applyGuardianProfile(guardian.getId(), row, colIndex, true);
                        created++;
                    }

                    String studentDoc = getCellString(row, colIndex, "studentDocumentNumber");
                    String relationship = getCellString(row, colIndex, "relationship");
                    if (studentDoc != null && !studentDoc.isBlank() && relationship != null) {
                        final User gRef = guardian;
                        userRepository.findByDocumentNumberAndInstitutionId(studentDoc, institutionId)
                                .ifPresent(student -> {
                                    if (!guardianRepository.existsByStudentIdAndGuardianId(student.getId(), gRef.getId())) {
                                        guardianRepository.save(StudentGuardian.builder()
                                                .institutionId(institutionId)
                                                .studentId(student.getId())
                                                .guardianId(gRef.getId())
                                                .relationship(relationship)
                                                .build());
                                    }
                                });
                    }
                } catch (Exception e) {
                    failed++;
                    Map<String, Object> err = new LinkedHashMap<>();
                    err.put("row", i + 1);
                    err.put("message", e.getMessage());
                    errors.add(err);
                }
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (IOException e) {
            throw new BusinessException("Failed to read file: " + e.getMessage(), HttpStatus.BAD_REQUEST, "FILE_READ_ERROR");
        }

        return finishJob(job, processed, created, updated, failed, errors);
    }

    public byte[] getStudentTemplate() throws IOException {
        return buildTemplate(STUDENT_HEADERS);
    }

    public byte[] getTeacherTemplate() throws IOException {
        return buildTemplate(TEACHER_HEADERS);
    }

    public byte[] getGuardianTemplate() throws IOException {
        return buildTemplate(GUARDIAN_HEADERS);
    }

    private void applyStudentCommonFields(User user, Row row, Map<String, Integer> colIndex) {
        user.setDocumentNumber(getCellString(row, colIndex, "documentNumber"));
        user.setDocumentType(getCellString(row, colIndex, "documentType"));
        user.setPhone(getCellString(row, colIndex, "phone"));
        user.setAddress(getCellString(row, colIndex, "address"));
        user.setGender(getCellString(row, colIndex, "gender"));
        user.setDistrict(getCellString(row, colIndex, "district"));
        user.setCity(getCellString(row, colIndex, "city"));
        String birthDate = getCellString(row, colIndex, "birthDate");
        if (birthDate != null && !birthDate.isBlank()) {
            try {
                user.setBirthDate(LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } catch (Exception ignored) {}
        }
    }

    private void applyStudentProfile(Long userId, Row row, Map<String, Integer> colIndex, boolean isNew) {
        StudentProfile profile = isNew
                ? StudentProfile.builder().userId(userId).build()
                : studentProfileRepository.findByUserId(userId).orElse(StudentProfile.builder().userId(userId).build());
        profile.setStudentCode(getCellString(row, colIndex, "studentCode"));
        profile.setBloodType(getCellString(row, colIndex, "bloodType"));
        profile.setAllergies(getCellString(row, colIndex, "allergies"));
        profile.setMedicalNotes(getCellString(row, colIndex, "medicalNotes"));
        studentProfileRepository.save(profile);
    }

    private void applyTeacherProfile(Long userId, Row row, Map<String, Integer> colIndex, boolean isNew) {
        TeacherProfile profile = isNew
                ? TeacherProfile.builder().userId(userId).build()
                : teacherProfileRepository.findByUserId(userId).orElse(TeacherProfile.builder().userId(userId).build());
        profile.setEmployeeCode(getCellString(row, colIndex, "employeeCode"));
        profile.setSpecialty(getCellString(row, colIndex, "specialty"));
        String hireDate = getCellString(row, colIndex, "hireDate");
        if (hireDate != null && !hireDate.isBlank()) {
            try {
                profile.setHireDate(LocalDate.parse(hireDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } catch (Exception ignored) {}
        }
        teacherProfileRepository.save(profile);
    }

    private void applyGuardianProfile(Long userId, Row row, Map<String, Integer> colIndex, boolean isNew) {
        GuardianProfile profile = isNew
                ? GuardianProfile.builder().userId(userId).build()
                : guardianProfileRepository.findByUserId(userId).orElse(GuardianProfile.builder().userId(userId).build());
        profile.setOccupation(getCellString(row, colIndex, "occupation"));
        profile.setWorkplace(getCellString(row, colIndex, "workplace"));
        guardianProfileRepository.save(profile);
    }

    private ImportJobResponse finishJob(ImportJob job, int processed, int created, int updated, int failed,
                                        List<Map<String, Object>> errors) {
        job.setStatus("DONE");
        job.setProcessed(processed);
        job.setCreated(created);
        job.setUpdated(updated);
        job.setFailed(failed);
        try {
            job.setErrorDetails(errors.isEmpty() ? null : MAPPER.writeValueAsString(errors));
        } catch (Exception ignored) {}
        importJobRepository.save(job);
        return toResponse(job, errors);
    }

    private byte[] buildTemplate(String[] headers) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("template");
            Row headerRow = sheet.createRow(0);
            CellStyle style = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            style.setFont(font);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(style);
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    private Map<String, Integer> buildColumnIndex(Row headerRow) {
        Map<String, Integer> index = new HashMap<>();
        for (Cell cell : headerRow) {
            if (cell != null && cell.getCellType() == CellType.STRING) {
                index.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
            }
        }
        return index;
    }

    private String getCellString(Row row, Map<String, Integer> colIndex, String colName) {
        Integer idx = colIndex.get(colName);
        if (idx == null) return null;
        Cell cell = row.getCell(idx);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                yield d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }

    private ImportJobResponse toResponse(ImportJob job) {
        List<Map<String, Object>> errors = List.of();
        if (job.getErrorDetails() != null) {
            try {
                errors = MAPPER.readValue(job.getErrorDetails(), new TypeReference<>() {});
            } catch (Exception ignored) {}
        }
        return toResponse(job, errors);
    }

    private ImportJobResponse toResponse(ImportJob job, List<Map<String, Object>> errors) {
        return ImportJobResponse.builder()
                .jobId(job.getId())
                .type(job.getType())
                .status(job.getStatus())
                .processed(job.getProcessed())
                .created(job.getCreated())
                .updated(job.getUpdated())
                .failed(job.getFailed())
                .errors(errors)
                .createdAt(job.getCreatedAt())
                .build();
    }
}
