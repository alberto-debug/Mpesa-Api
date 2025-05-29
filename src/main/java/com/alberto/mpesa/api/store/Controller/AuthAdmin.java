package com.alberto.mpesa.api.store.Controller;

import com.alberto.mpesa.api.store.DTO.LoginRequestDTO;
import com.alberto.mpesa.api.store.Repository.AdminRepository;
import com.alberto.mpesa.api.store.Repository.RoleRepository;
import com.alberto.mpesa.api.store.domain.model.Admin;
import com.alberto.mpesa.api.store.infra.Security.TokenService;
import com.nimbusds.oauth2.sdk.TokenRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/admin")
public class AuthAdmin {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginRequestDTO body){

        Admin admin = this.adminRepository.findByEmail(body.email())
                .orElseThrow(()-> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(body.Password(), body.email())){
            return ResponseEntity.badRequest().body("Invalid Credentials");
        }

        boolean isAdmin = admin.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (!isAdmin){
            return ResponseEntity.status(403).body("Access denied");
        }
        return null;
    }

}
