package back.vybz.gateway_service.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;


import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtProvider {

    private final Environment env;

    private SecretKey key;

    /**
     * 애플리케이션 시작 시 secret-key 기반으로 SecretKey 객체 생성
     * → 이후 모든 토큰 검증에서 이 key 재사용
     */
    @PostConstruct
    public void init() {
        String secret = Objects.requireNonNull(env.getProperty("JWT.secret-key"));
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if(claims.getExpiration().before(new Date())) {
                log.warn("❌ Token expired at {}", claims.getExpiration());
                return false;
            }

            return true;
        } catch (Exception e) {
            log.warn("❌ Invalid token: {}", e.getMessage());
            return false;
        }
    }

    public String getUserUuid(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("user_uuid", String.class);
    }

    public String getBuskerUuid(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("busker_uuid", String.class);
    }

    public SecretKey getSignKey() {
        return key;
    }
}