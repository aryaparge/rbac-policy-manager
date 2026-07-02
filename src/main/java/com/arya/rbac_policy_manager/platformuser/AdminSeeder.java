package com.arya.rbac_policy_manager.platformuser;

import com.arya.rbac_policy_manager.platformuser.entity.PlatformRole;
import com.arya.rbac_policy_manager.platformuser.entity.PlatformUser;
import com.arya.rbac_policy_manager.platformuser.repository.PlatformUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "ChangeMe123!";

    private final PlatformUserRepository platformUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (platformUserRepository.findByUsername(DEFAULT_ADMIN_USERNAME).isEmpty()) {
            PlatformUser admin = new PlatformUser();
            admin.setUsername(DEFAULT_ADMIN_USERNAME);
            admin.setPasswordHash(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
            admin.setEnabled(true);
            admin.setRole(PlatformRole.ADMIN);

            platformUserRepository.save(admin);

            log.warn("Seeded default admin user '{}' with a default password. Change it immediately.",
                    DEFAULT_ADMIN_USERNAME);
        }
    }
}
