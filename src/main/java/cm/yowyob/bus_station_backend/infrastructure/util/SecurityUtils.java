package cm.yowyob.bus_station_backend.infrastructure.util;

import cm.yowyob.bus_station_backend.domain.model.User;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.UserEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class SecurityUtils {

    public static Mono<User> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(authentication -> authentication != null && authentication.isAuthenticated())
                .map(authentication -> (User) authentication.getPrincipal());
    }

    // --- Helper pour récupérer l'ID de l'utilisateur connecté ---
    public static Mono<UUID> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth != null && auth.isAuthenticated())
                .map(auth -> auth.getPrincipal())
                .cast(User.class)
                .map(User::getUserId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)));
    }
}
