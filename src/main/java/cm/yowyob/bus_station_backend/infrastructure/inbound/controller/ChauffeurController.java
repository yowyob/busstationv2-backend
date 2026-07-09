package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.disponibilite.DisponibiliteResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.user.ChauffeurRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.user.UserResponseCreatedDTO;
import cm.yowyob.bus_station_backend.application.dto.user.UserResponseDTO;
import cm.yowyob.bus_station_backend.application.port.in.DisponibiliteUseCase;
import cm.yowyob.bus_station_backend.application.port.in.UserUseCase;
import cm.yowyob.bus_station_backend.application.validation.OnCreate;
import cm.yowyob.bus_station_backend.application.validation.OnUpdate;
import cm.yowyob.bus_station_backend.domain.exception.RegistrationException;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static cm.yowyob.bus_station_backend.infrastructure.util.SecurityUtils.getCurrentUserId;

import java.util.UUID;

/**
 * Controller REST sous /chauffeur.
 * NOTE : les chauffeurs portent le rôle EMPLOYE dans le système d'auth (pas de RoleType.CHAUFFEUR).
 * Leur statut "chauffeur" est dérivé de la table chauffeurs (FK user_id).
 */
@RestController
@RequestMapping("/chauffeur")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ChauffeurController {

    private final UserUseCase userUseCase;
    private final DisponibiliteUseCase disponibiliteUseCase;

    @Operation(summary = "Lister les chauffeurs d'une agence")
    @GetMapping("/agence/{agenceId}")
    public Flux<UserResponseDTO> getByAgence(@PathVariable UUID agenceId) {
        return userUseCase.getChauffeursByAgenceId(agenceId);
    }

    @Operation(summary = "Vérifier la disponibilité d'un chauffeur à une date/heure")
    @GetMapping("/{id}/disponibilite")
    public Mono<ResponseEntity<DisponibiliteResponseDTO>> getDisponibilite(
            @PathVariable UUID id,
            @RequestParam String date,
            @RequestParam(required = false) String heure) {
        return disponibiliteUseCase.checkChauffeurDisponibilite(id, date, heure)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Créer un chauffeur")
    @PostMapping
    public Mono<ResponseEntity<UserResponseCreatedDTO>> create(
            @RequestBody @Validated(OnCreate.class) ChauffeurRequestDTO dto) {
        return getCurrentUserId()
                .flatMap(currentUserId -> userUseCase.createChauffeur(dto, dto.getAgenceVoyageId(), currentUserId))
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c))
                .onErrorResume(RegistrationException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()));
    }

    @Operation(summary = "Modifier un chauffeur")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserResponseCreatedDTO>> update(
            @PathVariable UUID id,
            @RequestBody @Validated(OnUpdate.class) ChauffeurRequestDTO dto) {
        return getCurrentUserId()
                .flatMap(currentUserId -> userUseCase.updateChauffeur(id, dto, currentUserId))
                .map(ResponseEntity::ok)
                .onErrorResume(ResourceNotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Supprimer un chauffeur")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return getCurrentUserId()
                .flatMap(currentUserId -> userUseCase.deleteChauffeur(id, currentUserId))
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .onErrorResume(ResourceNotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }
}