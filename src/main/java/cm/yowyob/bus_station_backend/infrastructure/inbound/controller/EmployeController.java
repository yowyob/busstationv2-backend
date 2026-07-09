package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.user.EmployeRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.user.EmployeResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.user.UserResponseCreatedDTO;
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

@RestController
@RequestMapping("/employe")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class EmployeController {

    private final UserUseCase userUseCase;

    @Operation(summary = "Lister les employés d'une agence")
    @GetMapping("/agence/{agenceId}")
    public Flux<EmployeResponseDTO> getByAgence(@PathVariable UUID agenceId) {
        return userUseCase.getEmployesByAgenceId(agenceId);
    }

    @Operation(summary = "Créer un employé")
    @PostMapping
    public Mono<ResponseEntity<UserResponseCreatedDTO>> create(
            @RequestBody @Validated(OnCreate.class) EmployeRequestDTO dto) {
        return getCurrentUserId()
                .flatMap(currentUserId -> userUseCase.createEmploye(dto, dto.getAgenceVoyageId(), currentUserId))
                .map(e -> ResponseEntity.status(HttpStatus.CREATED).body(e))
                .onErrorResume(RegistrationException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()));
    }

    @Operation(summary = "Modifier un employé")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserResponseCreatedDTO>> update(
            @PathVariable UUID id,
            @RequestBody @Validated(OnUpdate.class) EmployeRequestDTO dto) {
        return getCurrentUserId()
                .flatMap(currentUserId -> userUseCase.updateEmploye(id, dto, currentUserId))
                .map(ResponseEntity::ok)
                .onErrorResume(ResourceNotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Supprimer un employé")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return getCurrentUserId()
                .flatMap(currentUserId -> userUseCase.deleteEmploye(id, currentUserId))
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .onErrorResume(ResourceNotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }
}

