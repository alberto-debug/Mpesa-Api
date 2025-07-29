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
import java.util.List;

@Service
public class TokenService {

    @Value("${KEY}")
    private String secret;

    private static final String ISSUER = "login-api";

    public String generateToken(Admin admin) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            // ✅ Extract role names from admin object
            List<String> roles = admin.getRoles().stream()
                    .map(role -> role.getName()) // e.g. "ROLE_ADMIN"
                    .toList();

            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(admin.getEmail())
                    .withClaim("roles", roles)  // ✅ Add roles to token
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while authenticating");
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Error Validating token");
        }
    }

    public String getEmailFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now()
                .plusHours(2)
                .toInstant(ZoneOffset.of("-03:00"));
    }
}
