package com.langassessment.config;

import com.langassessment.entity.Language;
import com.langassessment.entity.User;
import com.langassessment.repository.LanguageRepository;
import com.langassessment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeData(
            UserRepository userRepository,
            LanguageRepository languageRepository) {
        return args -> {
            try {
                initializeLanguages(languageRepository);
                initializeAdminUser(userRepository);
                log.info("Database initialization completed successfully");
            } catch (Exception e) {
                log.error("Error during database initialization", e);
            }
        };
    }

    private void initializeLanguages(LanguageRepository languageRepository) {
        String[][] languages = {
                {"en", "English"},
                {"es", "Spanish"},
                {"fr", "French"},
                {"de", "German"},
                {"zh", "Mandarin Chinese"},
                {"ja", "Japanese"}
        };

        for (String[] lang : languages) {
            if (languageRepository.findByCode(lang[0]).isEmpty()) {
                Language language = Language.builder()
                        .code(lang[0])
                        .name(lang[1])
                        .build();
                languageRepository.save(language);
                log.info("Created language: {}", lang[1]);
            }
        }
    }

    private void initializeAdminUser(UserRepository userRepository) {
        String adminEmail = "admin@langassessment.com";
        String adminPassword = "password";

        if (userRepository.findByEmail(adminEmail).isPresent()) {
            User existingAdmin = userRepository.findByEmail(adminEmail).get();
            String encodedPassword = passwordEncoder.encode(adminPassword);
            existingAdmin.setPasswordHash(encodedPassword);
            userRepository.save(existingAdmin);
            log.info("Updated admin user password");
        } else {
            String encodedPassword = passwordEncoder.encode(adminPassword);
            User adminUser = User.builder()
                    .email(adminEmail)
                    .name("System Administrator")
                    .passwordHash(encodedPassword)
                    .role(User.UserRole.ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(adminUser);
            log.info("Created admin user with email: {}", adminEmail);
        }
    }
}
