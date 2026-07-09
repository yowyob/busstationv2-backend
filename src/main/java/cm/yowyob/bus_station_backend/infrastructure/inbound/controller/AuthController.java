package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.auth.*;
import cm.yowyob.bus_station_backend.application.dto.user.AuthentificationDTO;
import cm.yowyob.bus_station_backend.application.dto.user.UserDTO;
import cm.yowyob.bus_station_backend.application.dto.user.UserResponseCreatedDTO;
import cm.yowyob.bus_station_backend.application.dto.user.UserResponseDTO;
import cm.yowyob.bus_station_backend.application.port.in.UserUseCase;
import cm.yowyob.bus_station_backend.application.port.out.UserPersistencePort;
import cm.yowyob.bus_station_backend.application.validation.OnCreate;
import cm.yowyob.bus_station_backend.application.validation.OnUpdate;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.model.User;
import cm.yowyob.bus_station_backend.infrastructure.config.security.JwtService;
import cm.yowyob.bus_station_backend.infrastructure.config.security.TokenStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static cm.yowyob.bus_station_backend.infrastructure.util.SecurityUtils.getCurrentUserId;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentification", description = "Inscription, connexion, gestion du token, mot de passe")
public class AuthController {

    private final UserUseCase userUseCase;
    private final UserPersistencePort userPersistencePort;
    private final JwtService jwtService;
    private final TokenStoreService tokenStoreService;
    private final ReactiveAuthenticationManager authenticationManager;

    // ============================================================
    // INSCRIPTION & CONNEXION
    // ============================================================

    @PostMapping("/register")
    @Operation(summary = "Inscription d'un utilisateur (USAGER ou AGENCE_VOYAGE)")
    public Mono<ResponseEntity<UserResponseCreatedDTO>> register(
            @RequestBody @Validated(OnCreate.class) UserDTO user) {
        return userUseCase.registerUser(user)
                .map(created -> new ResponseEntity<>(created, HttpStatus.CREATED));
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Connexion : retourne accessToken + refreshToken")
    public Mono<ResponseEntity<AuthTokensDTO>> login(@RequestBody AuthentificationDTO authDTO) {
         return authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(authDTO.username(), authDTO.password()))
                .flatMap(auth -> jwtService.generateJwt(authDTO.username())
                        .flatMap(userResp -> {
                            String refreshToken = jwtService.generateRefreshToken(authDTO.username());
                            String refreshJti = jwtService.extractTokenId(refreshToken);

                            return tokenStoreService.storeRefreshToken(
                                            authDTO.username(),
                                            refreshJti,
                                            Duration.ofMillis(JwtService.REFRESH_TOKEN_VALIDITY_MS))
                                    .thenReturn(AuthTokensDTO.builder()
                                            .accessToken(userResp.getToken())
                                            .refreshToken(refreshToken)
                                            .expiresIn(JwtService.ACCESS_TOKEN_VALIDITY_MS / 1000)
                                            .user(userResp)
                                            .build());
                        }))
                .map(ResponseEntity::ok)
                .onErrorResume(BadCredentialsException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }

    // ============================================================
    // REFRESH & LOGOUT
    // ============================================================

    @PostMapping("/refresh")
    @Operation(summary = "Régénère un access token à partir d'un refresh token valide")
    public Mono<ResponseEntity<AuthTokensDTO>> refresh(@RequestBody @Valid RefreshTokenRequestDTO body) {
        String refreshToken = body.refreshToken();

        return Mono.fromCallable(() -> {
                    // Validation cryptographique + type
                    String type = jwtService.extractTokenType(refreshToken);
                    if (!"refresh".equals(type)) {
                        throw new BadCredentialsException("Le token fourni n'est pas un refresh token");
                    }
                    String username = jwtService.extractUsername(refreshToken);
                    if (!jwtService.isTokenValid(refreshToken, username)) {
                        throw new BadCredentialsException("Refresh token expiré ou invalide");
                    }
                    return new String[]{username, jwtService.extractTokenId(refreshToken)};
                })
                .flatMap(arr -> {
                    String username = arr[0];
                    String jti = arr[1];
                    // Vérification présence en Redis
                    return tokenStoreService.isRefreshTokenValid(username, jti)
                            .flatMap(valid -> {
                                if (!Boolean.TRUE.equals(valid)) {
                                    return Mono.error(new BadCredentialsException("Refresh token révoqué ou inconnu"));
                                }
                                return jwtService.generateJwt(username)
                                        .map(userResp -> AuthTokensDTO.builder()
                                                .accessToken(userResp.getToken())
                                                .refreshToken(refreshToken) // pas de rotation pour l'instant
                                                .expiresIn(JwtService.ACCESS_TOKEN_VALIDITY_MS / 1000)
                                                .user(userResp)
                                                .build());
                            });
                })
                .map(ResponseEntity::ok)
                .onErrorResume(BadCredentialsException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Déconnexion : blackliste l'access token courant + révoque le refresh token")
    public Mono<ResponseEntity<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) RefreshTokenRequestDTO body) {

        Mono<Void> blacklistAccess = Mono.empty();
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            try {
                String jti = jwtService.extractTokenId(accessToken);
                Date exp = jwtService.extractExpiration(accessToken);
                long remainingMs = exp.getTime() - System.currentTimeMillis();
                if (jti != null && remainingMs > 0) {
                    blacklistAccess = tokenStoreService
                            .blacklistAccessToken(jti, Duration.ofMillis(remainingMs)).then();
                }
            } catch (Exception ignored) { /* token corrompu, on ignore */ }
        }

        Mono<Void> revokeRefresh = Mono.empty();
        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
            try {
                String username = jwtService.extractUsername(body.refreshToken());
                String jti = jwtService.extractTokenId(body.refreshToken());
                revokeRefresh = tokenStoreService.revokeRefreshToken(username, jti).then();
            } catch (Exception ignored) { /* token corrompu, on ignore */ }
        }

        return blacklistAccess
                .then(revokeRefresh)
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }

    // ============================================================
    // PROFIL UTILISATEUR CONNECTÉ
    // ============================================================

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Profil de l'utilisateur connecté")
    public Mono<ResponseEntity<UserResponseDTO>> me() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getPrincipal())
                .cast(User.class)
                .flatMap(u -> userUseCase.getUserProfile(u.getUserId()))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PutMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Modification du profil de l'utilisateur connecté")
    public Mono<ResponseEntity<UserResponseDTO>> updateMe(
            @RequestBody @Validated(OnUpdate.class) UserDTO userDTO) {
        return getCurrentUserId()
                .flatMap(userId -> userUseCase.updateUserProfile(userId, userDTO))
                .map(ResponseEntity::ok)
                .onErrorResume(ResourceNotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @PutMapping("/me/password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Changer son propre mot de passe")
    public Mono<ResponseEntity<Void>> changePassword(@RequestBody @Valid ChangePasswordRequestDTO body) {
        return getCurrentUserId()
                .flatMap(userId -> userUseCase.changePassword(userId, body.oldPassword(), body.newPassword()))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(BadCredentialsException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }

    // ============================================================
    // FORGOT / RESET PASSWORD
    // ============================================================

    @PostMapping("/forgot-password")
    @Operation(summary = "Demande de réinitialisation : génère un token, l'envoie par email (TODO Kafka)")
    public Mono<ResponseEntity<Map<String, String>>> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequestDTO body) {
        return userUseCase.initiatePasswordReset(body.email())
                .map(token -> {
                    Map<String, String> resp = new HashMap<>();
                    resp.put("message", "Un email de réinitialisation a été envoyé si l'adresse existe");
                    // En DEV uniquement : retourne le token pour faciliter les tests
                    resp.put("debugToken", token);
                    return ResponseEntity.ok(resp);
                })
                // On retourne 200 même si l'email n'existe pas, pour éviter l'énumération
                .onErrorResume(ResourceNotFoundException.class, e -> Mono.just(
                        ResponseEntity.ok(Map.of("message",
                                "Un email de réinitialisation a été envoyé si l'adresse existe"))));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialisation du mot de passe via le token reçu par email")
    public Mono<ResponseEntity<Void>> resetPassword(@RequestBody @Valid ResetPasswordRequestDTO body) {
        return userUseCase.resetPassword(body.token(), body.newPassword())
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(BadCredentialsException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()));
    }
}