package org.pgno20.medimart.config;

import org.pgno20.medimart.model.User;
import org.pgno20.medimart.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class SuperAdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SuperAdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        String superAdminEmail = "superadmin@medimart.com";

        if (!userRepository.existsByEmail(superAdminEmail)) {
            User superAdmin = new User();
            superAdmin.setFullName("Super Administrator");
            superAdmin.setEmail(superAdminEmail);
            superAdmin.setPassword(passwordEncoder.encode("SuperAdmin@123"));
            superAdmin.setDob(LocalDate.of(2000, 1, 1));
            superAdmin.setGender("Other");
            superAdmin.setRoleName("ROLE_ADMIN");
            superAdmin.setActive(true);

            userRepository.save(superAdmin);
            System.out.println("[SuperAdminInitializer] Default super admin created: " + superAdminEmail);
        } else {
            System.out.println("[SuperAdminInitializer] Super admin already exists.");
        }
    }
}
