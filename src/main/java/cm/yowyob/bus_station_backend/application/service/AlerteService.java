package cm.yowyob.bus_station_backend.application.service;

import cm.yowyob.bus_station_backend.application.dto.alerte.AlerteCreateDTO;
import cm.yowyob.bus_station_backend.application.port.in.AlerteUseCase;
import cm.yowyob.bus_station_backend.application.port.out.AlertePersistencePort;
import cm.yowyob.bus_station_backend.domain.model.AlerteAgence;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlerteService implements AlerteUseCase {

    private final AlertePersistencePort alertePersistencePort;

    @Override
    public Mono<AlerteAgence> createAlerte(AlerteCreateDTO dto, UUID gareId, UUID bsmId) {
        AlerteAgence alerte = AlerteAgence.builder()
                .idAlerte(UUID.randomUUID())
                .gareId(gareId)
                .agenceId(dto.getAgenceId())
                .bsmId(bsmId)
                .type(dto.getType())
                .message(dto.getMessage())
                .isLu(false)
                .createdAt(LocalDateTime.now())
                .build();

        return alertePersistencePort.save(alerte);
    }

    @Override
    public Flux<AlerteAgence> getAlertesByGare(UUID gareId) {
        return alertePersistencePort.findByGareId(gareId);
    }

    @Override
    public Flux<AlerteAgence> getAlertesByAgence(UUID agenceId) {
        return alertePersistencePort.findByAgenceId(agenceId);
    }

    @Override
    public Mono<AlerteAgence> marquerLu(UUID alerteId) {
        return alertePersistencePort.findById(alerteId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Alerte introuvable : " + alerteId)))
                .flatMap(alerte -> {
                    if (alerte.isLu()) {
                        return Mono.just(alerte); // idempotent : déjà lue
                    }
                    alerte.setLu(true);
                    alerte.setLuAt(LocalDateTime.now());
                    return alertePersistencePort.update(alerte);
                });
    }
}