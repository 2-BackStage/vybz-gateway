package back.vybz.gateway_service.common.jwt;

import back.vybz.gateway_service.common.exception.BaseException;
import back.vybz.gateway_service.common.exception.BaseResponseStatus;
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
        Claims claims = validateAndExtractClaims(token);
        String uuid = claims.get("user_uuid", String.class);
        if (uuid == null) {
            throw new BaseException(BaseResponseStatus.NOT_USER_UUID);
        }
        return uuid;
    }

    public String getBuskerUuid(String token) {
        Claims claims = validateAndExtractClaims(token);
        String uuid = claims.get("busker_uuid", String.class);
        if (uuid == null) {
            throw new BaseException(BaseResponseStatus.NOT_BUSKER_UUID);
        }
        return uuid;
    }

    public String getUuidFromEither(String token) {
        Claims claims = validateAndExtractClaims(token);
        String userUuid = claims.get("user_uuid", String.class);
        String buskerUuid = claims.get("busker_uuid", String.class);

        if (userUuid != null && buskerUuid != null) {
            throw new BaseException(BaseResponseStatus.DUPLICATE_UUID);
        }
        if (userUuid != null) return userUuid;
        if (buskerUuid != null) return buskerUuid;

        throw new BaseException(BaseResponseStatus.NO_UUID_FOUND);
    }

    public Claims validateAndExtractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public SecretKey getSignKey() {
        return key;
    }
}