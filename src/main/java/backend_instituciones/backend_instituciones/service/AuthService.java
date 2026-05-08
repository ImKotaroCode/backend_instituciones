package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.User;
import backend_instituciones.backend_instituciones.dto.response.UserResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserResponse me(Long userId, Long institutionId) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getInstitutionId().equals(institutionId))
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        return userService.toResponse(user);
    }
}
