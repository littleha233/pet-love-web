package com.petlove.weblove.security;

import com.petlove.weblove.config.AppAuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final AppAuthProperties authProperties;

    public JwtService(AppAuthProperties authProperties) {
        this.authProperties = authProperties;
        this.secretKey = Keys.hmacShaKeyFor(authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createUserAccessToken(long userId) {
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("typ", JwtTokenType.USER_ACCESS.name())
            .issuedAt(new Date())
            .expiration(Date.from(Instant.now().plusSeconds(authProperties.getAccessTokenSeconds())))
            .signWith(secretKey)
            .compact();
    }

    public String createAdminAccessToken(long adminId, String role) {
        return Jwts.builder()
            .subject(String.valueOf(adminId))
            .claim("typ", JwtTokenType.ADMIN_ACCESS.name())
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(Date.from(Instant.now().plusSeconds(authProperties.getAccessTokenSeconds())))
            .signWith(secretKey)
            .compact();
    }

    public ParsedToken parseAccessToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        long subjectId = Long.parseLong(claims.getSubject());
        JwtTokenType tokenType = JwtTokenType.valueOf((String) claims.get("typ"));
        String role = claims.get("role", String.class);
        return new ParsedToken(subjectId, tokenType, role);
    }
}
