package kroryi.bus2.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import kroryi.bus2.entity.apikey.ApiKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenUtil {

    @Value("${kroryi.jwt.secret}")
    private String secret;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // secret 값이 Base64로 인코딩되어 있다면 디코딩하여 사용
        if (secret.contains("=")) {
            byte[] decodedKey = Base64.getDecoder().decode(secret);
            if (decodedKey.length < 32) {
                throw new IllegalArgumentException("JWT secret must be at least 32 bytes after decoding");
            }
            this.secretKey = Keys.hmacShaKeyFor(decodedKey);
        } else {
            // Base64 인코딩되지 않은 경우, 그대로 UTF-8로 변환하여 사용
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 32) {
                throw new IllegalArgumentException("JWT secret must be at least 32 bytes");
            }
            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        }
    }

    // JWT 생성 메서드
    public String generateToken(ApiKey key) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 86400000); // 예: 1일 후

        return Jwts.builder()
                .setSubject(key.getId().toString())
                .claim("name", key.getUser_name())
                .claim("allowedIp", key.getAllowedIp())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 파싱 메서드
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtException("JWT expired", e);
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT", e);
        }
    }
}
