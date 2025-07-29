package com.alberto.mpesa.api.store.Controller;

import com.alberto.mpesa.api.store.DTO.ManagerCreationDTO;
import com.alberto.mpesa.api.store.DTO.ResponseDTO;
import com.alberto.mpesa.api.store.Repository.AdminRepository;
import com.alberto.mpesa.api.store.Repository.RoleRepository;
import com.alberto.mpesa.api.store.domain.Role.Role;
import com.alberto.mpesa.api.store.domain.model.Admin;
import com.alberto.mpesa.api.store.infra.Security.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/admin/managers")
public class AdminController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/create")
    public ResponseEntity<?> createManager(@RequestBody ManagerCreationDTO body,
                                           @RequestHeader("Authorization") String token) {
        String adminEmail = tokenService.getEmailFromToken(token.replace("Bearer ", ""));
        log.info("Extracted email from token: {}", adminEmail);

        Admin admin = adminRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        boolean isAdmin = admin.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(403).body(new ResponseDTO("Access Denied: only admins can create managers", null));
        }

        if (adminRepository.findByEmail(body.email()).isPresent()) {
            return ResponseEntity.badRequest().body(new ResponseDTO("Email already in use", null));
        }

        Role staffRole = roleRepository.findByName("ROLE_STAFF")
                .orElseThrow(() -> new RuntimeException("ROLE_STAFF not found"));

        Admin manager = new Admin();
        manager.setName(body.name());
        manager.setEmail(body.email());
        manager.setPassword(passwordEncoder.encode(body.password()));
        manager.setRoles(new HashSet<>(Collections.singletonList(staffRole)));

        adminRepository.save(manager);
        log.info("Manager created with email: {}", body.email());

        return ResponseEntity.ok(new ResponseDTO("Manager created Successfully", null));
    }

    @GetMapping("/list")
    public ResponseEntity<?> listManager(@RequestHeader("Authorization") String token) {
        String adminEmail = tokenService.getEmailFromToken(token.replace("Bearer ", ""));
        log.info("Extracted email from token: {}", adminEmail);

        Admin admin = adminRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        boolean isAdmin = admin.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(403).body(new ResponseDTO("Access denied: only admins can list managers", null));
        }

        List<Admin> managers = adminRepository.findAll().stream()
                .filter(admin1 -> admin1.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_STAFF")))
                .toList();

        log.info("Found {} total users", adminRepository.findAll().size());
        log.info("Found {} total staff managers", managers.size());

        if (managers.isEmpty()) {
            log.info("No managers found, returning empty array");
            return ResponseEntity.ok(new ResponseDTO("No managers found", "[]"));
        }

        String managersData = "[" + managers.stream()
                .map(m -> {
                    String info = m.getId() + ":" + m.getName() + ":" + m.getEmail();
                    log.info("Manager info: {}", info);
                    return info;
                })
                .collect(Collectors.joining("|")) + "]";

        log.info("Returning managers data: {}", managersData);

        return ResponseEntity.ok(new ResponseDTO("Managers retrieved successfully", managersData));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteManagers(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        String adminEmail = tokenService.getEmailFromToken(token.replace("Bearer ", ""));
        log.info("Extracted email from token: {}", adminEmail);

        Admin admin = adminRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found with email: " + adminEmail));

        boolean isAdmin = admin.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(403).body(new ResponseDTO("Access denied: only admins can delete managers", null));
        }

        Admin manager = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        boolean isStaff = manager.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_STAFF"));
        if (!isStaff) {
            return ResponseEntity.badRequest().body(new ResponseDTO("User is not a manager", null));
        }

        adminRepository.delete(manager);
        log.info("Deleted manager with ID: {}", id);

        return ResponseEntity.ok(new ResponseDTO("Manager deleted Successfully", null));
    }
}
