package com.alberto.mpesa.api.store.infra.Security;

import com.alberto.mpesa.api.store.domain.model.Admin;
import com.auth0.jwt.exceptions.JWTCreationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    @Value("${key}")
    private String secret;
    private String generateToken(Admin admin){
        try {

        }catch (JWTCreationException exception){
            throw new RuntimeException("Error while authenticating");
        }
    }
}
