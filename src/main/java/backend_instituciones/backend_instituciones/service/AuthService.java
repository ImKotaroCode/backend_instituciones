package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.User;
import backend_instituciones.backend_instituciones.dto.request.LoginRequest;
import backend_instituciones.backend_instituciones.dto.response.LoginResponse;
import backend_instituciones.backend_instituciones.dto.response.UserResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.repository.UserRepository;
import backend_instituciones.backend_instituciones.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository
                .findByEmailAndInstitutionId(request.getEmail().toLowerCase().trim(), request.getInstitutionId())
                .orElseThrow(() -> new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"));

        if (!user.isActive()) {
            throw new BusinessException("Account is deactivated", HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        String role = user.getRole().name();
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getInstitutionId(), role);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getInstitutionId(), role);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .role(role)
                        .institutionId(user.getInstitutionId())
                        .build())
                .build();
    }

    public LoginResponse refresh(String refreshToken) {
        if (!jwtService.isValid(refreshToken)) {
            throw new BusinessException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }

        Long userId = Long.valueOf(jwtService.extractUserId(refreshToken));
        Long institutionId = Long.valueOf(jwtService.extractInstitutionId(refreshToken));
        String role = jwtService.extractRole(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getInstitutionId(), role);
        String newRefreshToken = jwtService.generateRefreshToken(user.getId(), user.getInstitutionId(), role);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(3600)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .role(role)
                        .institutionId(user.getInstitutionId())
                        .build())
                .build();
    }

    public UserResponse me(Long userId, Long institutionId) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getInstitutionId().equals(institutionId))
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        return userService.toResponse(user);
    }
}
