package com.shift_cut.Config;

import com.shift_cut.Model.Enum.Role;
import com.shift_cut.Model.UserEntity;
import com.shift_cut.Repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class DataInitializer {

    private final UserEntityRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAdminUser() {
        return args -> {
            if (userRepository.findByEmail("admin@admin.com").isEmpty()) {
                UserEntity admin = UserEntity.builder()
                        .username("Admin")
                        .email("admin@admin.com")
                        .password(passwordEncoder.encode("Admin123"))
                        .role(Role.ADMIN)
                        .status(true)
                        .build();

                userRepository.save(admin);
                log.info(">>> Usuario ADMIN creado por defecto: admin@admin.com / Admin123");
                log.info(">>> Por seguridad, cambia la contrasena del ADMIN en produccion.");
            } else {
                log.info(">>> Usuario ADMIN ya existe, no se crea de nuevo.");
            }
        };
    }
}

