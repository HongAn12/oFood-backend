package com.example.demo.Service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtService {

    @Value("${security.jwt.secret}")        // >= 32 bytes cho HS256
    private String secret;

    @Value("${security.jwt.access-minutes:15}")
    private long accessMinutes;

    @Value("${security.jwt.refresh-days:14}")
    private long refreshDays;

    // clockSkew cho parser (giảm lỗi lệch vài giây giữa server/client)
    @Value("${security.jwt.clock-skew-seconds:30}")
    private long clockSkewSeconds;

    private Key key() {
        // secret nên là chuỗi ngẫu nhiên 32+ bytes (có thể là Base64)
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private JwtParser parser() {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .setAllowedClockSkewSeconds(clockSkewSeconds)
                .build();
    }

    /* ===================== GENERATE ===================== */

    public String generateAccess(Long userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("typ", "access")
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(Duration.ofMinutes(accessMinutes))))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefresh(Long userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("typ", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(Duration.ofDays(refreshDays))))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /* ===================== PARSE / VALIDATE ===================== */

    /** Trả userId nếu token hợp lệ, ném JwtException nếu không */
    public Long parseUser(String token) {
        Claims c = parser().parseClaimsJws(token).getBody();
        return Long.valueOf(c.getSubject());
    }

    /** Kiểm tra token hợp lệ (true/false, không ném lỗi) */
    public boolean isValid(String token) {
        try {
            parser().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** Kiểm tra có phải refresh token không */
    public boolean isRefreshToken(String token) {
        try {
            Claims c = parser().parseClaimsJws(token).getBody();
            Object typ = c.get("typ");
            return typ != null && "refresh".equals(typ.toString());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** Lấy toàn bộ claims (null nếu lỗi) */
    public Claims getClaimsSafely(String token) {
        try {
            return parser().parseClaimsJws(token).getBody();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
