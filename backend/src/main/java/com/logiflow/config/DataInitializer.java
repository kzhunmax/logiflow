package com.logiflow.config;

import com.logiflow.user.model.Role;
import com.logiflow.user.model.User;
import com.logiflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.super-admin.username}")
    private String superAdminUsername;

    @Value("${app.super-admin.email}")
    private String superAdminEmail;

    @Value("${app.super-admin.password}")
    private String superAdminPassword;

    @Value("${app.super-admin.full-name:Super Administrator}")
    private String superAdminFullName;

    @Bean
    CommandLineRunner initSuperAdmin() {
        return _ -> {
            if (userRepository.findByRole(Role.SUPER_ADMIN).isEmpty()) {
                log.info("No SUPER_ADMIN found. Creating initial super admin account...");

                User superAdmin = buildAdmin();
                userRepository.save(superAdmin);

                log.info("Super admin account created successfully.");
            } else {
                log.info("Super admin account already exists. Skipping initialization.");
            }
        };
    }

    private User buildAdmin() {
        return User.builder()
                .username(superAdminUsername)
                .email(superAdminEmail)
                .password(passwordEncoder.encode(superAdminPassword))
                .fullName(superAdminFullName)
                .role(Role.SUPER_ADMIN)
                .enabled(true)
                .build();
    }
}

