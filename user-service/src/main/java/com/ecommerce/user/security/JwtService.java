package com.ecommerce.user.security;

import com.ecommerce.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

@Component
@Log4j2
public class JwtService {

    private static final String CLAIM_ROLE = "role";
    private static final long CLOCK_SKEW_SECONDS = 30; // small tolerance

    @Value("${example.app.jwtSecret}")
    private String jwtSecret;

    @Value("${example.app.jwtRefreshSecret}")
    private String jwtRefreshSecret;

    @Value("${example.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${example.app.jwtRefreshExpirationMs}")
    private int jwtRefreshExpirationMs;

    private SecretKey jwtKey;
    private SecretKey jwtRefreshKey;

    @PostConstruct
    public void initKeys() {
        // Keep your behavior; just guard against empty secrets.
        if (isBlank(jwtSecret) || isBlank(jwtRefreshSecret)) {
            throw new IllegalStateException("JWT secrets must be configured (jwtSecret / jwtRefreshSecret).");
        }
        // HMAC keys (ensure length >= 32 chars for HS256/HS512 safety)
        jwtKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        jwtRefreshKey = Keys.hmacShaKeyFor(jwtRefreshSecret.getBytes(StandardCharsets.UTF_8));
        log.debug("JWT keys initialized.");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Generate Access Token from full User (includes role). */
    public String generateJwtToken(User user) {
        Objects.requireNonNull(user, "user must not be null");
        return Jwts.builder()
                .subject(user.getEmail())
                .claim(CLAIM_ROLE, user.getUserRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + (long) jwtExpirationMs))
                .signWith(jwtKey)
                .compact();
    }


    /** Generate Refresh Token (kept your role claim). */
    public String generateJwtRefreshToken(User user) {
        Objects.requireNonNull(user, "user must not be null");
        return Jwts.builder()
                .subject(user.getEmail())
                .claim(CLAIM_ROLE, user.getUserRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + (long) jwtRefreshExpirationMs))
                .signWith(jwtRefreshKey)
                .compact();
    }


    /** Extract username (subject) from Refresh Token. */
    public String getUserNameFromJwtRefreshToken(String token) {
        return Jwts.parser()
                .clockSkewSeconds(CLOCK_SKEW_SECONDS)
                .verifyWith(jwtRefreshKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /** Extract all claims from Access Token. */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .clockSkewSeconds(CLOCK_SKEW_SECONDS)
                .verifyWith(jwtKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    /** Validate Access Token signature/structure/expiry. */
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser()
                    .clockSkewSeconds(CLOCK_SKEW_SECONDS)
                    .verifyWith(jwtKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /** Validate Refresh Token signature/structure/expiry. */
    public boolean validateJwtRefreshToken(String token) {
        try {
            Jwts.parser()
                    .clockSkewSeconds(CLOCK_SKEW_SECONDS)
                    .verifyWith(jwtRefreshKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT refresh token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT refresh token: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Invalid JWT refresh signature: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT refresh token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT refresh claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
