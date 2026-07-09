package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.reservation.BilletDTO;
import cm.yowyob.bus_station_backend.application.dto.user.*;
import cm.yowyob.bus_station_backend.application.port.in.ReservationUseCase;
import cm.yowyob.bus_station_backend.application.port.in.UserUseCase;
import cm.yowyob.bus_station_backend.application.validation.OnCreate;
import cm.yowyob.bus_station_backend.application.validation.OnUpdate;
import cm.yowyob.bus_station_backend.domain.exception.RegistrationException;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.model.User;
import cm.yowyob.bus_station_backend.infrastructure.config.security.JwtService;
import static cm.yowyob.bus_station_backend.infrastructure.util.SecurityUtils.getCurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/utilisateur")
@AllArgsConstructor
@Slf4j
public class UserController {

        private final UserUseCase userUseCase;
        private final ReservationUseCase reservationUseCase;
        private final JwtService jwtService;
        private final ReactiveAuthenticationManager authenticationManager;

        @GetMapping("/test")
        @Operation(summary = "Test endpoint")
        public Mono<String> test() {
                return Mono.just("Hello World");
        }

        @Operation(summary = "Obtenir les informations d'un billet", description = "Cette méthode permet de récupérer toutes les informations liées à un billet, y compris les informations sur le passager et le voyage.", tags = {
                        "Billet" })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Billet trouvé et retourné avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BilletDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Le passager ou les informations associées n'ont pas été trouvés"),
                        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
        })
        @GetMapping("/billet/{idPassager}")
        @SecurityRequirement(name = "bearerAuth")
        public Mono<ResponseEntity<BilletDTO>> getBilletInformation(@PathVariable UUID idPassager) {
                return reservationUseCase.generateBillet(idPassager)
                                .map(ResponseEntity::ok)
                                .onErrorResume(ResourceNotFoundException.class,
                                                e -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()))
                                .onErrorResume(Exception.class, e -> Mono
                                                .just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
        }

        @PostMapping("/inscription")
        @Operation(summary = "Sign up a user")
        public Mono<ResponseEntity<UserResponseCreatedDTO>> inscription(@RequestBody @Validated(OnCreate.class) UserDTO user) {
                return userUseCase.registerUser(user)
                                .map(createdUser -> new ResponseEntity<>(createdUser, HttpStatus.CREATED));
        }

        @PostMapping(path = "/connexion", consumes = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Get a token for an user")
        public Mono<ResponseEntity<UserResponseDTO>> getToken(@RequestBody AuthentificationDTO authDTO) {
                return authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(authDTO.username(), authDTO.password()))
                                .flatMap(auth -> jwtService.generateJwt(authDTO.username()))
                                .map(userResponseDTO -> {
                                        return ResponseEntity.ok(userResponseDTO);
                                })
                                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
        }

        @GetMapping("/profil")
        @SecurityRequirement(name = "bearerAuth")
        @Operation(summary = "Get a user information by token")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User information", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public Mono<ResponseEntity<UserResponseDTO>> getProfile() {
                return ReactiveSecurityContextHolder.getContext()
                                .map(ctx -> ctx.getAuthentication().getPrincipal())
                                .cast(User.class)
                                .flatMap(user -> userUseCase.getUserProfile(user.getUserId()))
                                .map(ResponseEntity::ok)
                                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
        }

        @PostMapping("/chauffeur")
        @SecurityRequirement(name = "bearerAuth")
        @Operation(summary = "Create chauffeur agence voyage")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Chauffeur created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseCreatedDTO.class))),
                        @ApiResponse(responseCode = "409", description = "User already exists")
        })
        public Mono<ResponseEntity<UserResponseCreatedDTO>> createChauffeur(
                        @RequestBody @Validated(OnCreate.class) ChauffeurRequestDTO dto) {
                return getCurrentUserId()
                                .flatMap(currentUserId -> userUseCase.createChauffeur(dto, dto.getAgenceVoyageId(),
                                                currentUserId))
                                .map(chauffeur -> ResponseEntity.status(HttpStatus.CREATED).body(chauffeur))
                                .onErrorResume(RegistrationException.class,
                                                e -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()));
        }

        @PutMapping("/chauffeur/{chauffeurId}")
        @SecurityRequirement(name = "bearerAuth")
        @Operation(summary = "Update chauffeur agence voyage")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Chauffeur updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseCreatedDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Chauffeur not found")
        })
        public Mono<ResponseEntity<UserResponseCreatedDTO>> updateChauffeur(
                        @PathVariable UUID chauffeurId,
                        @RequestBody @Validated(OnUpdate.class) ChauffeurRequestDTO dto) {
                return getCurrentUserId()
                                .flatMap(currentUserId -> userUseCase.updateChauffeur(chauffeurId, dto, currentUserId))
                                .map(ResponseEntity::ok)
                                .onErrorResume(ResourceNotFoundException.class,
                                                e -> Mono.just(ResponseEntity.notFound().build()));
        }

        @GetMapping("/chauffeurs/{agenceId}")
        @SecurityRequirement(name = "bearerAuth")
        @Operation(summary = "Get chauffeurs by agence ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Chauffeurs found successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserResponseDTO.class)))),
                        @ApiResponse(responseCode = "404", description = "Agence not Found")
        })
        public Flux<UserResponseDTO> getChauffeursByAgenceId(@PathVariable UUID agenceId) {
                return userUseCase.getChauffeursByAgenceId(agenceId);
        }

        @DeleteMapping("/chauffeur/{chauffeurId}")
        @SecurityRequirement(name = "bearerAuth")
        @Operation(summary = "Delete chauffeur agence voyage")
        public Mono<ResponseEntity<Void>> deleteChauffeur(@PathVariable UUID chauffeurId) {
                return getCurrentUserId()
                                .flatMap(currentUserId -> userUseCase.deleteChauffeur(chauffeurId, currentUserId))
                                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                                .onErrorResume(ResourceNotFoundException.class,
                                                e -> Mono.just(ResponseEntity.notFound().build()));
        }

        @PostMapping("/employe")
        @SecurityRequirement(name = "bearerAuth")
        @Operation(summary = "Create employe agence voyage")
        public Mono<ResponseEntity<UserResponseCreatedDTO>> createEmploye(@RequestBody @Validated(OnCreate.class) EmployeRequestDTO dto) {
                return getCurrentUserId()
                                .flatMap(currentUserId -> userUseCase.createEmploye(dto, dto.getAgenceVoyageId(),
                                                currentUserId))
                                .map(employe -> ResponseEntity.status(HttpStatus.CREATED).body(employe))
                                .onErrorResume(RegistrationException.class,
                                                e -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(null)));
        }

        @PutMapping("/employe/{employeId}")
        @SecurityRequirement(name = "bearerAuth")
        @Operation(summary = "Update employe agence voyage")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Employe updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseCreatedDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Employe not found")
        })
        public Mono<ResponseEntity<UserResponseCreatedDTO>> updateEmploye(
                        @PathVariable UUID employeId,
                        @RequestBody @Validated(OnUpdate.class) EmployeRequestDTO dto) {
                return getCurrentUserId()
                                .flatMap(currentUserId -> userUseCase.updateEmploye(employeId, dto, currentUserId))
                                .map(ResponseEntity::ok)
                                .onErrorResume(ResourceNotFoundException.class,
                                                e -> Mono.just(ResponseEntity.notFound().build()));
        }

        @GetMapping("/employes/{agenceId}")
        @SecurityRequirement(name = "bearerAuth")
        @Operation(summary = "Get employes by agence ID")
        public Flux<EmployeResponseDTO> getEmployesByAgenceId(@PathVariable UUID agenceId) {
                return userUseCase.getEmployesByAgenceId(agenceId);
        }

        @DeleteMapping("/employe/{employeId}")
        @SecurityRequirement(name = "bearerAuth")
        @Operation(summary = "Delete employe agence voyage")
        public Mono<ResponseEntity<Void>> deleteEmploye(@PathVariable UUID employeId) {
                return getCurrentUserId()
                                .flatMap(currentUserId -> userUseCase.deleteEmploye(employeId, currentUserId))
                                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                                .onErrorResume(ResourceNotFoundException.class,
                                                e -> Mono.just(ResponseEntity.notFound().build()));
        }
}