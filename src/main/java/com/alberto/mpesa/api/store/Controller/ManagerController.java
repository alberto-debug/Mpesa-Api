package com.alberto.mpesa.api.store.Controller;

import com.alberto.mpesa.api.store.DTO.LoginRequestDTO;
import com.alberto.mpesa.api.store.DTO.ResponseDTO;
import com.alberto.mpesa.api.store.Repository.AdminRepository;
import com.alberto.mpesa.api.store.domain.model.Admin;
import com.alberto.mpesa.api.store.infra.Security.TokenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/login")
@AllArgsConstructor
public class ManagerController {

    private final AdminRepository adminRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> managerLogin(@RequestBody LoginRequestDTO body) {

        Admin manager = adminRepository.findByEmail(body.email())
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (!passwordEncoder.matches(body.password(), manager.getPassword())) {
            return ResponseEntity.badRequest().body(new ResponseDTO("Invalid Credentials", null));
        }

        boolean isAllowed = manager.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_STAFF") || role.getName().equals("ROLE_ADMIN"));

        if (!isAllowed) {
            return ResponseEntity.status(403).body(new ResponseDTO("Access denied", null));
        }

        String token = tokenService.generateToken(manager);
        log.info("Manager logged in with email: {}", manager.getEmail());

        return ResponseEntity.ok(new ResponseDTO("Manager logged in Successfully", token));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> managerDashboard(@RequestHeader("Authorization") String token) {
        String email = tokenService.getEmailFromToken(token.replace("Bearer ", ""));
        log.info("Extracted email from token: {}", email);

        Admin user = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_STAFF") || role.getName().equals("ROLE_ADMIN"));
        if (!isAuthorized) {
            return ResponseEntity.status(403).body(new ResponseDTO("Access denied: Requires ROLE_STAFF or ROLE_ADMIN", null));
        }

        return ResponseEntity.ok(new ResponseDTO("Welcome to Manager Dashboard, " + user.getName(), null));
    }
}
