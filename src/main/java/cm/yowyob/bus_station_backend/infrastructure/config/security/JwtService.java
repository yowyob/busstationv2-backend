package cm.yowyob.bus_station_backend.infrastructure.config.security;

import cm.yowyob.bus_station_backend.application.dto.user.UserResponseDTO;
import cm.yowyob.bus_station_backend.application.mapper.UserMapper;
import cm.yowyob.bus_station_backend.application.port.out.UserPersistencePort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final UserPersistencePort userPersistencePort;
    private final UserMapper userMapper;

    // Durées des tokens
    public static final long ACCESS_TOKEN_VALIDITY_MS = 32L * 24 * 60 * 60 * 1000L; // 32 jours
    public static final long REFRESH_TOKEN_VALIDITY_MS = 7 * 24 * 60 * 60 * 1000L;  // 7 jours

    /**
     * Génère un access token (durée courte).
     */
    public String generateAccessToken(String username, Map<String, Object> extraClaims) {
        long now = System.currentTimeMillis();
        Map<String, Object> claims = new HashMap<>(extraClaims == null ? Map.of() : extraClaims);
        claims.put("type", "access");

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date(now))
                .expiration(new Date(now + ACCESS_TOKEN_VALIDITY_MS))
                .signWith(getKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Génère un refresh token (durée longue, payload minimal).
     * On y stocke aussi un JTI (JWT ID) pour permettre la révocation côté Redis.
     */
    public String generateRefreshToken(String username) {
        long now = System.currentTimeMillis();
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date(now))
                .expiration(new Date(now + REFRESH_TOKEN_VALIDITY_MS))
                .signWith(getKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Méthode rétrocompatible utilisée par d'anciennes parties du code.
     * Génère uniquement un access token (ne crée plus de refresh token ici).
     */
    public String generateToken(String username, Map<String, Object> extraClaims) {
        return generateAccessToken(username, extraClaims);
    }

    /**
     * Génère un access token enrichi (roles + userId) à partir du username.
     * Utilisé par /auth/login et /auth/refresh.
     */
    public Mono<UserResponseDTO> generateJwt(String username) {
        return userPersistencePort.findByUsername(username)
                .switchIfEmpty(Mono.error(new RuntimeException("Utilisateur non trouvé pour la génération du token")))
                .map(user -> {
                    Map<String, Object> extraClaims = new HashMap<>();
                    extraClaims.put("roles", user.getRoles());
                    extraClaims.put("userId", user.getUserId());

                    String token = generateAccessToken(user.getUsername(), extraClaims);
                    return userMapper.toResponseDTO(user, token);
                });
    }

    private SecretKey getKey() {
        String secretString = "votre_cle_secrete_tres_longue_et_securisee_123456";
        return Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return getAllClaims(token, Claims::getSubject);
    }

    public String extractTokenId(String token) {
        return getAllClaims(token, Claims::getId);
    }

    public String extractTokenType(String token) {
        return getAllClaims(token, claims -> (String) claims.get("type"));
    }

    public Date extractExpiration(String token) {
        return getAllClaims(token, Claims::getExpiration);
    }

    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return getAllClaims(token, Claims::getExpiration).before(new Date());
    }

    private <T> T getAllClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}