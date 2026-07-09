package cm.yowyob.bus_station_backend.infrastructure.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SecurityContextRepository implements ServerSecurityContextRepository {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenStoreService tokenStoreService;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .flatMap(authHeader -> {
                    String authToken = authHeader.substring(7);

                    // 1. Vérifier que le token n'est pas blacklisté
                    String jti;
                    try {
                        jti = jwtService.extractTokenId(authToken);
                    } catch (Exception e) {
                        return Mono.empty(); // token corrompu : on laisse Spring renvoyer 401
                    }

                    return tokenStoreService.isAccessTokenBlacklisted(jti)
                            .flatMap(blacklisted -> {
                                if (Boolean.TRUE.equals(blacklisted)) {
                                    return Mono.empty(); // token révoqué
                                }
                                Authentication auth = new UsernamePasswordAuthenticationToken(authToken, authToken);
                                return authenticationManager.authenticate(auth)
                                        .map(SecurityContextImpl::new)
                                        .onErrorResume(e -> Mono.empty());
                            });
                });
    }
}

