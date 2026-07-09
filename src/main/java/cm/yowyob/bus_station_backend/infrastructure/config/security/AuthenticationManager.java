package cm.yowyob.bus_station_backend.infrastructure.config.security;

import cm.yowyob.bus_station_backend.domain.enums.RoleType;
import cm.yowyob.bus_station_backend.domain.model.User;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.UserEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.UserR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;
    private final UserR2dbcRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String credentials = authentication.getCredentials().toString();
        String principal = authentication.getPrincipal().toString();

        // Déterminer le type d'authentification en vérifiant si credentials ressemble à un JWT
        if (looksLikeJwt(credentials)) {
            return authenticateWithJwt(credentials);
        } else {
            return authenticateWithPassword(principal, credentials);
        }
    }

    /**
     * Vérifie si la chaîne ressemble à un token JWT (format: xxx.yyy.zzz)
     */
    private boolean looksLikeJwt(String token) {
        return token != null && token.split("\\.").length == 3;
    }

    /**
     * Authentification par JWT Token
     */
    private Mono<Authentication> authenticateWithJwt(String token) {
        try {
            String username = jwtService.extractUsername(token);

            if (username == null) {
                return Mono.error(new BadCredentialsException("Invalid token: unable to extract username"));
            }

            if (!jwtService.isTokenValid(token, username)) {
                return Mono.error(new BadCredentialsException("Token expired or invalid"));
            }

            return userRepository.findByUsername(username)
                    .switchIfEmpty(Mono.error(new BadCredentialsException("User not found")))
                    .map(this::mapToDomainUser)
                    .map(user -> {
                        var authorities = user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                                .collect(Collectors.toList());

                        return new UsernamePasswordAuthenticationToken(
                                user,
                                token,
                                authorities);
                    });

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return Mono.error(new BadCredentialsException("Token has expired"));
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            return Mono.error(new BadCredentialsException("Malformed token"));
        } catch (io.jsonwebtoken.security.SignatureException e) {
            return Mono.error(new BadCredentialsException("Invalid token signature"));
        } catch (Exception e) {
            return Mono.error(new BadCredentialsException("Invalid authentication token: " + e.getMessage()));
        }
    }

    /**
     * Authentification par Username/Password
     */
    private Mono<Authentication> authenticateWithPassword(String username, String password) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new BadCredentialsException("User not found")))
                .flatMap(userEntity -> {
                    if (passwordEncoder.matches(password, userEntity.getPassword())) {
                        User user = mapToDomainUser(userEntity);
                        var authorities = user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                                .collect(Collectors.toList());

                        return Mono.just(new UsernamePasswordAuthenticationToken(
                                user,
                                password,
                                authorities));
                    } else {
                        return Mono.error(new BadCredentialsException("Invalid credentials"));
                    }
                });
    }

    /**
     * Mappe une UserEntity vers un User (modèle de domaine)
     */
    private User mapToDomainUser(UserEntity entity) {
        List<RoleType> roles = Arrays.stream(entity.getRoles().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(RoleType::valueOf)
                .collect(Collectors.toList());

        return User.builder()
                .userId(entity.getUserId())
                .nom(entity.getNom())
                .prenom(entity.getPrenom())
                .genre(entity.getGenre())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .telNumber(entity.getTelNumber())
                .roles(roles)
                .businessActorType(entity.getBusinessActorType())
                .address(entity.getAddress())
                .idcoordonneeGPS(entity.getIdcoordonneeGPS())
                .build();
    }
}