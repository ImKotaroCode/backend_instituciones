package backend_instituciones.backend_instituciones;

import backend_instituciones.backend_instituciones.domain.entity.AdminProfile;
import backend_instituciones.backend_instituciones.domain.entity.User;
import backend_instituciones.backend_instituciones.domain.enums.Role;
import backend_instituciones.backend_instituciones.repository.AdminProfileRepository;
import backend_instituciones.backend_instituciones.repository.UserRepository;
import backend_instituciones.backend_instituciones.service.SseService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class BackendInstitucionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendInstitucionesApplication.class, args);
    }

    @Bean
    SseHeartbeat sseHeartbeat(SseService sseService) {
        return new SseHeartbeat(sseService);
    }

    public static class SseHeartbeat {
        private final SseService sseService;

        SseHeartbeat(SseService sseService) {
            this.sseService = sseService;
        }

        @Scheduled(fixedDelay = 30_000)
        public void beat() {
            sseService.sendHeartbeat();
        }
    }

    @Bean
    CommandLineRunner init(UserRepository userRepository, AdminProfileRepository adminProfileRepository,
                           PasswordEncoder passwordEncoder) {
        return args -> {
            Long defaultInstitutionId = 1L;
            String adminEmail = "admin@default.com";
            if (!userRepository.existsByEmailAndInstitutionId(adminEmail, defaultInstitutionId)) {
                User admin = User.builder()
                    .institutionId(defaultInstitutionId)
                    .name("Administrator")
                    .firstName("Administrator")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("Admin123!"))
                    .role(Role.ADMIN)
                    .isActive(true)
                    .build();
                admin = userRepository.save(admin);
                adminProfileRepository.save(AdminProfile.builder().userId(admin.getId()).build());
            }
        };
    }
}
