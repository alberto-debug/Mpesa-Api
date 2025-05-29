package com.alberto.mpesa.api.store.infra.Security;

import com.alberto.mpesa.api.store.domain.model.Admin;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${key}")
    private String secret;
    private String generateToken(Admin admin){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            String token  = JWT.create()
                    .withIssuer("login-api")
                    .withSubject(admin.getEmail())
                    .withExpiresAt(this.generateExpirationtoken())
                    .sign(algorithm);
            return token;

        }catch (JWTCreationException exception){
            throw new RuntimeException("Error while authenticating");
        }
    }

    private String validateToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
                    return JWT.require(algorithm)
                            .withIssuer("login-api")
                            .build()
                            .verify(token)
                            .getSubject();

        }catch (JWTVerificationException exception){
            throw new RuntimeException("Error Validating token");
        }
    }

    private Instant generateExpirationtoken(){
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-3:00"));
    }
}
