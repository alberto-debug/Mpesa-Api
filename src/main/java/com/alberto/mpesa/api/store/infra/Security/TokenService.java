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

    @Value("${KEY}")
    private String secret;
    public String generateToken(Admin admin){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            String token  = JWT.create()
                    .withIssuer("login-api")
                    .withSubject(admin.getEmail())
                    .withExpiresAt(this.generateExpirationDate())
                    .sign(algorithm);
            return token;

        }catch (JWTCreationException exception){
            throw new RuntimeException("Error while authenticating");
        }
    }


    public String validateToken(String token){
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

    public String getEmailFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("school-system")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }


    private Instant generateExpirationDate(){
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
