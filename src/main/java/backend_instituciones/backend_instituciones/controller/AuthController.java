package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Returns current user profile.
     * Auth is Supabase-managed — frontend sends Supabase JWT.
     * SupabaseAuthFilter resolves user from DB and populates TenantContext.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        return ResponseEntity.ok(authService.me(TenantContext.getUserId(), TenantContext.getInstitutionId()));
    }
}
