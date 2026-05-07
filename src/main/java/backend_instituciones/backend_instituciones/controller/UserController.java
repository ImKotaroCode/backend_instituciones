package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.domain.enums.Role;
import backend_instituciones.backend_instituciones.dto.request.CreateUserRequest;
import backend_instituciones.backend_instituciones.dto.request.UpdateUserRequest;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.list(TenantContext.getInstitutionId(), page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(required = false) Role role,
                                    @RequestParam(required = false) Boolean active,
                                    @RequestParam(required = false) String q,
                                    @RequestParam(required = false) String documentNumber,
                                    @RequestParam(required = false) Long classroomId,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.search(TenantContext.getInstitutionId(), role, active,
                q, documentNumber, classroomId, page, size));
    }

    @PostMapping(value = "/{id}/photo", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadPhoto(@PathVariable Long id,
                                         @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.uploadPhoto(id, TenantContext.getInstitutionId(), file));
    }

    @DeleteMapping("/{id}/photo")
    public ResponseEntity<?> deletePhoto(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deletePhoto(id, TenantContext.getInstitutionId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(userService.get(id, TenantContext.getInstitutionId()));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.create(TenantContext.getInstitutionId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, TenantContext.getInstitutionId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id, TenantContext.getInstitutionId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable Long id,
                                        @RequestBody Map<String, String> body) {
        Role role = Role.valueOf(body.get("role"));
        return ResponseEntity.ok(userService.changeRole(id, TenantContext.getInstitutionId(), role));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        return ResponseEntity.ok(userService.activate(id, TenantContext.getInstitutionId()));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deactivate(id, TenantContext.getInstitutionId()));
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id,
                                           @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(userService.resetPassword(id, TenantContext.getInstitutionId(), body.get("newPassword")));
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<?> getFullProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.get(id, TenantContext.getInstitutionId()));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<?> updateFullProfile(@PathVariable Long id,
                                               @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(userService.get(id, TenantContext.getInstitutionId()));
    }

    @GetMapping("/{id}/activity")
    public ResponseEntity<?> activity(@PathVariable Long id,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.getActivity(id, TenantContext.getInstitutionId(), page, size));
    }
}
