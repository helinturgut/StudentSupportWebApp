package com.studentsupport.config;

import com.studentsupport.entity.Role;
import com.studentsupport.entity.RoleName;
import com.studentsupport.entity.User;
import com.studentsupport.repository.RoleRepository;
import com.studentsupport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed-admin.email:}")
    private String seedAdminEmail;

    @Value("${app.seed-admin.password:}")
    private String seedAdminPassword;

    @Override
    public void run(String... args) {
        seedRoles();
        seedAdminUser();
    }

    private void seedRoles() {
        for (RoleName name : RoleName.values()) {
            if (roleRepository.findByRoleName(name).isEmpty()) {
                roleRepository.save(Role.builder().roleName(name).build());
            }
        }
    }

    private void seedAdminUser() {
        if (!StringUtils.hasText(seedAdminEmail) || !StringUtils.hasText(seedAdminPassword)) {
            log.warn("SEED_ADMIN_EMAIL / SEED_ADMIN_PASSWORD not set — skipping admin account seeding");
            return;
        }
        if (userRepository.existsByEmail(seedAdminEmail)) {
            return;
        }
        Role adminRole = roleRepository.findByRoleName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found after seeding"));
        userRepository.save(User.builder()
                .fullName("Admin")
                .email(seedAdminEmail)
                .passwordHash(passwordEncoder.encode(seedAdminPassword))
                .role(adminRole)
                .build());
    }
}
