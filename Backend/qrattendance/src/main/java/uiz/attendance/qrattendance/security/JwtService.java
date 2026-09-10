package uiz.attendance.qrattendance.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uiz.attendance.qrattendance.service.IssuedToken;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

/**
 * Issues and validates HS256-signed JWTs for the single configured admin
 * account.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(@Value("${jwt.secret}") String base64Secret,
                       @Value("${jwt.expiration}") long expirationMillis) {
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
        this.expirationMillis = expirationMillis;
    }

    public IssuedToken generateToken(String username) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(expirationMillis);
        String token = Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
        return new IssuedToken(token, expiresAt);
    }

    /**
     * Returns the token's subject (username) if the signature is valid and
     * the token has not expired, or empty otherwise. Never throws.
     */
    public Optional<String> validateAndExtractUsername(String token) {
        try {
            String username = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Optional.ofNullable(username);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
