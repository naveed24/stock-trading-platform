package com.example.auth_service.security;

import com.example.auth_service.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// This service is responsible for generating JWT tokens.
@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        return Jwts.builder()
                // Subject is usually the main identity of the user.
                .subject(user.getEmail())

                // Extra claims stored inside token.
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())

                // Token created time.
                .issuedAt(new Date())

                // Token expiry time.
                .expiration(new Date(System.currentTimeMillis() + expiration))

                // Sign token using secret key.
                .signWith(getSigningKey(), Jwts.SIG.HS256)

                .compact();


    }

}
