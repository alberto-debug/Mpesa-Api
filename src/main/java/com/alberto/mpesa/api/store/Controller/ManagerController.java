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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/login")
@AllArgsConstructor
public class ManagerController {

    private final AdminRepository adminRepository;
    private final TokenService tokenService;
    private PasswordEncoder passwordEncoder;


    @PostMapping("/login")
    public ResponseEntity<?> managerLogin(@RequestBody LoginRequestDTO body){

        Admin manager =  adminRepository.findByEmail(body.email())
                .orElseThrow(()->new RuntimeException("Manager not found"));

        if (!passwordEncoder.matches(body.password(), manager.getPassword())){
            return ResponseEntity.badRequest().body(new ResponseDTO("Invalid Credentials", null));
        }

        boolean isManager = manager.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_MANAGER"));

        if (isManager){
            return ResponseEntity.status(403).body(new ResponseDTO("Access denied", null));

        }

        String token = tokenService.generateToken(manager);
        log.info("Manager logged with email: {} " , manager.getEmail());

        return null;
    }
}
