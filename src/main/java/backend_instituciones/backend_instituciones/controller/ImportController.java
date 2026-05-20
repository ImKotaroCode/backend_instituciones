package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/imports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRACION')")
public class ImportController {

    private final ImportService service;

    @GetMapping("/{jobId}")
    public ResponseEntity<?> getJob(@PathVariable String jobId) {
        return ResponseEntity.ok(service.getJob(jobId, TenantContext.getInstitutionId()));
    }

    @PostMapping(value = "/students", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importStudents(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.accepted()
                .body(service.importStudents(TenantContext.getInstitutionId(), classroomId, academicYear, file));
    }

    @GetMapping("/students/template")
    public ResponseEntity<byte[]> studentTemplate() throws IOException {
        byte[] bytes = service.getStudentTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("students_template.xlsx").build().toString())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @PostMapping(value = "/teachers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importTeachers(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.accepted()
                .body(service.importTeachers(TenantContext.getInstitutionId(), file));
    }

    @GetMapping("/teachers/template")
    public ResponseEntity<byte[]> teacherTemplate() throws IOException {
        byte[] bytes = service.getTeacherTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("teachers_template.xlsx").build().toString())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @PostMapping(value = "/guardians", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importGuardians(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.accepted()
                .body(service.importGuardians(TenantContext.getInstitutionId(), file));
    }

    @GetMapping("/guardians/template")
    public ResponseEntity<byte[]> guardianTemplate() throws IOException {
        byte[] bytes = service.getGuardianTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("guardians_template.xlsx").build().toString())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}
