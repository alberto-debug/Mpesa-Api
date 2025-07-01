package com.alberto.mpesa.api.store.Controller;

import com.alberto.mpesa.api.store.DTO.ManagerCreationDTO;
import com.alberto.mpesa.api.store.DTO.ResponseDTO;
import com.alberto.mpesa.api.store.Repository.AdminRepository;
import com.alberto.mpesa.api.store.Repository.RoleRepository;
import com.alberto.mpesa.api.store.domain.Role.Role;
import com.alberto.mpesa.api.store.domain.model.Admin;
import com.alberto.mpesa.api.store.infra.Security.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/admin/managers")
public class AdminRole {

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
                                           @RequestHeader("Authorization") String token){


        String adminEmail = tokenService.getEmailFromToken(token.replace("Bearer ", " "));
        Admin admin = adminRepository.findByEmail(adminEmail)
                .orElseThrow(()-> new RuntimeException("Admin not found"));

        boolean isAdmin = admin.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        if (!isAdmin){
            return ResponseEntity.status(404).body(new ResponseDTO("Access Denied: only admins can create managers", null));
        }

        if (adminRepository.findByEmail(body.email()).isPresent()){
            return ResponseEntity.badRequest().body(new ResponseDTO("Email already in use", null));
        }

        Role managerRole = roleRepository.findByName("MANAGER_ROLE")
                .orElseThrow(()-> new RuntimeException("ROLE_MANAGER not found"));

        Admin manager = new Admin();
        manager.setName(body.name());
        manager.setEmail(body.name());
        manager.setPassword(passwordEncoder.encode(body.password()));
        manager.setRoles(new HashSet<>(Collections.singletonList(managerRole)));

        adminRepository.save(manager);
        log.info("Manager created with email: {}", body.email());

        return ResponseEntity.ok(new ResponseDTO("Manager created Successfully", null));
    }

    @GetMapping("/list")
    public ResponseEntity<?> listManager(@RequestHeader("Authorization") String token){

        String adminEmail = tokenService.getEmailFromToken(token.replace("Bearer ", " "));
        Admin admin = adminRepository.findByEmail(adminEmail)
                .orElseThrow(()-> new RuntimeException("Admin not found"));

        boolean isAdmin =  admin.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        if (!isAdmin){
            return ResponseEntity.status(403).body(new ResponseDTO("Access denied: only admins can list managers", null));
        }

        List<Admin> managers = adminRepository.findAll().stream()
                .filter(admin1 -> admin1.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_MANAGER")))
                .toList();

        //Debug log
        log.info("found {} total users: " , adminRepository.findAll().size());
        log.info("Found {} total managers: " , managers.size());

        //Data formating
        if (managers.isEmpty()){
            log.info("Not managers found, returning empty array");
            return ResponseEntity.ok(new ResponseDTO("No managers found ", "[]"));
        }

        return null;
    }
}
