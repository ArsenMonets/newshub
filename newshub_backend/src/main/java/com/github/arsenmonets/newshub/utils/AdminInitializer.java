package com.github.arsenmonets.newshub.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.github.arsenmonets.newshub.models.UserEntity;
import com.github.arsenmonets.newshub.models.UserRole;
import com.github.arsenmonets.newshub.repositories.UserRepository;

import java.util.logging.Logger;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger logger = Logger.getLogger(AdminInitializer.class.getName());

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.login}")
    private String adminLogin;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.email}")
    private String adminEmail;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!userRepository.existsByLogin(adminLogin)) {
            UserEntity admin = new UserEntity();
            admin.setLogin(adminLogin);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail(adminEmail);
            admin.setRole(UserRole.ADMIN);
            admin.setBlocked(false);

            userRepository.save(admin);
            logger.info("Admin user '" + adminLogin + "' created successfully");
        } else {
            logger.info("Admin user '" + adminLogin + "' already exists");
        }
    }
}
