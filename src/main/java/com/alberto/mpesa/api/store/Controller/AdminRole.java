package com.alberto.mpesa.api.store.Controller;

import com.alberto.mpesa.api.store.DTO.ManagerCreationDTO;
import com.alberto.mpesa.api.store.Repository.AdminRepository;
import com.alberto.mpesa.api.store.Repository.RoleRepository;
import com.alberto.mpesa.api.store.domain.model.Admin;
import com.alberto.mpesa.api.store.infra.Security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    }




}
