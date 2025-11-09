package com.pillmate.pillmate.Config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.pillmate.pillmate.Domain.AuthProvider;
import com.pillmate.pillmate.Domain.User;
import com.pillmate.pillmate.Repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pillmate.seed-user.enabled", havingValue = "true", matchIfMissing = true)
public class SeedUserInitializer implements ApplicationRunner {

    private static final String DEFAULT_EMAIL = "user@example.com";
    private static final String DEFAULT_PASSWORD = "Abcd1234!";
    private static final String DEFAULT_NAME = "테스트 사용자";

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(DEFAULT_EMAIL)) {
            return;
        }

        User user = User.builder()
                .name(DEFAULT_NAME)
                .email(DEFAULT_EMAIL)
                .password("")
                .termsOfService(true)
                .privacyPolicy(true)
                .dataUsage(false)
                .provider(AuthProvider.LOCAL)
                .providerId(DEFAULT_EMAIL)
                .build();

        user.encodePassword(DEFAULT_PASSWORD);
        userRepository.save(user);
        log.info("Seed user created: {}", DEFAULT_EMAIL);
    }
}

