package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupRoleSetup implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        setupRole("admin");
        setupRole("user");
    }

    private void setupRole(String roleName) {
        // Check if the role exists
        if (roleRepository.findByName(roleName).isEmpty()) {
            // Create and save the role if it does not exist
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            System.out.println("Role '" + roleName + "' created.");
        } else {
            System.out.println("Role '" + roleName + "' already exists.");
        }
    }

}
