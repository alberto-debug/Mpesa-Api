package com.alberto.mpesa.api.store.Controller;

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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RestController("/auth/admin")
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
    public ResponseEntity<Admin> adminLogin)@RequestBody

}
