package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationSetupInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        setupRole("ADMIN");
        setupRole("USER");
        setupAdminUser();
    }

    private void setupRole(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            log.info("Role '{}' created.", roleName);
        } else {
            log.info("Role '{}' already exists.", roleName);
        }
    }

    private void setupAdminUser() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setRoles(new HashSet<>(Set.of(
                    roleRepository.findByName("ADMIN").orElseThrow(() -> new IllegalStateException("Admin role not found"))
            )));
            userRepository.save(adminUser);
            log.info("Admin user created.");
        } else {
            log.info("Admin user already exists.");
        }
    }

}
