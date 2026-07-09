package cm.yowyob.bus_station_backend.infrastructure.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Gestion centralisée de la persistance Redis des tokens :
 *  - Refresh tokens (whitelist : un refresh token n'est valide que s'il est en Redis)
 *  - Access tokens blacklist (un access token blacklisté est rejeté même s'il n'est pas expiré)
 *  - Password reset tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenStoreService {

    private final ReactiveStringRedisTemplate redis;

    private static final String REFRESH_PREFIX = "auth:refresh:";        // auth:refresh:{username}:{jti} -> "1"
    private static final String BLACKLIST_PREFIX = "auth:blacklist:";    // auth:blacklist:{jti} -> "1"
    private static final String PWD_RESET_PREFIX = "auth:pwd-reset:";    // auth:pwd-reset:{token} -> {username}

    // ---------- Refresh tokens ----------

    public Mono<Boolean> storeRefreshToken(String username, String jti, Duration ttl) {
        String key = REFRESH_PREFIX + username + ":" + jti;
        return redis.opsForValue().set(key, "1", ttl);
    }

    public Mono<Boolean> isRefreshTokenValid(String username, String jti) {
        String key = REFRESH_PREFIX + username + ":" + jti;
        return redis.hasKey(key);
    }

    public Mono<Long> revokeRefreshToken(String username, String jti) {
        String key = REFRESH_PREFIX + username + ":" + jti;
        return redis.delete(key);
    }

    // ---------- Access token blacklist ----------

    public Mono<Boolean> blacklistAccessToken(String jti, Duration ttl) {
        // ttl = durée de vie restante du token
        if (ttl.isNegative() || ttl.isZero()) return Mono.just(true);
        String key = BLACKLIST_PREFIX + jti;
        return redis.opsForValue().set(key, "1", ttl);
    }

    public Mono<Boolean> isAccessTokenBlacklisted(String jti) {
        if (jti == null) return Mono.just(false);
        String key = BLACKLIST_PREFIX + jti;
        return redis.hasKey(key);
    }

    // ---------- Password reset tokens ----------

    public Mono<Boolean> storePasswordResetToken(String token, String username, Duration ttl) {
        String key = PWD_RESET_PREFIX + token;
        return redis.opsForValue().set(key, username, ttl);
    }

    public Mono<String> consumePasswordResetToken(String token) {
        String key = PWD_RESET_PREFIX + token;
        return redis.opsForValue().get(key)
                .flatMap(username -> redis.delete(key).thenReturn(username));
    }
}