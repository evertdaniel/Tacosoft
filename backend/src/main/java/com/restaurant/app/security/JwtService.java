package com.restaurant.app.security;

import com.restaurant.app.auth.model.AppUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** JWT token generation and validation service. Implements SPEC-AUTH-001 */
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-minutes}")
    private long expirationMinutes;

    /**
     * Generate JWT token for authenticated user. Claims include: sub, username, role,
     * restaurantRoles, exp
     */
    public String generateToken(AppUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getId().toString());
        claims.put("username", user.getUsername());
        claims.put("role", user.getPrimaryRole().getName());
        claims.put(
                "restaurantRoles",
                user.getRestaurantRoles().stream()
                        .map(
                                rr ->
                                        Map.of(
                                                "restaurantId", rr.getRestaurantId().toString(),
                                                "role", rr.getRole().getName()))
                        .collect(Collectors.toList()));

        Instant now = Instant.now();
        Instant expiration = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /** Validate JWT token and extract claims. Returns null if token is invalid or expired. */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            return null;
        }
    }

    /** Extract user ID from token. */
    public String extractUserId(String token) {
        Claims claims = validateToken(token);
        return claims != null ? claims.get("sub", String.class) : null;
    }

    /** Extract username from token. */
    public String extractUsername(String token) {
        Claims claims = validateToken(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    /** Extract restaurant roles from token. */
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> extractRestaurantRoles(String token) {
        Claims claims = validateToken(token);
        return claims != null ? (List<Map<String, String>>) claims.get("restaurantRoles") : null;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
