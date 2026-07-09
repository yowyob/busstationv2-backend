package cm.yowyob.bus_station_backend.application.service;

import cm.yowyob.bus_station_backend.application.dto.voyage.*;
import cm.yowyob.bus_station_backend.application.mapper.VoyageBrouillonMapper;
import cm.yowyob.bus_station_backend.application.port.in.VoyageBrouillonUseCase;
import cm.yowyob.bus_station_backend.application.port.in.VoyageUseCase;
import cm.yowyob.bus_station_backend.application.port.out.VoyageBrouillonPersistencePort;
import cm.yowyob.bus_station_backend.domain.enums.Amenities;
import cm.yowyob.bus_station_backend.domain.enums.StatutBrouillon;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.model.VoyageBrouillon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class VoyageBrouillonService implements VoyageBrouillonUseCase {

    private final VoyageBrouillonPersistencePort persistencePort;
    private final VoyageBrouillonMapper mapper;
    private final VoyageUseCase voyageUseCase;

    @Override
    public Mono<VoyageBrouillonResponseDTO> create(VoyageBrouillonCreateDTO dto) {
        VoyageBrouillon brouillon = mapper.toDomain(dto);
        recalculerStatut(brouillon);
        return persistencePort.save(brouillon).map(mapper::toResponse);
    }

    @Override
    public Mono<VoyageBrouillonResponseDTO> getById(UUID id) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Brouillon non trouvé : " + id)))
                .map(mapper::toResponse);
    }

    @Override
    public Mono<VoyageBrouillonResponseDTO> update(UUID id, VoyageBrouillonUpdateDTO dto) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Brouillon non trouvé : " + id)))
                .flatMap(existing -> {
                    if (existing.getStatutBrouillon() == StatutBrouillon.CONVERTI) {
                        return Mono.error(new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.BAD_REQUEST,
                                "Impossible de modifier un brouillon CONVERTI"));
                    }
                    mapper.applyUpdate(existing, dto);
                    recalculerStatut(existing);
                    return persistencePort.save(existing);
                })
                .map(mapper::toResponse);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Brouillon non trouvé : " + id)))
                .flatMap(b -> persistencePort.deleteById(id));
    }

    @Override
    public Flux<VoyageBrouillonResponseDTO> listByAgence(UUID agenceId, String statut) {
        return persistencePort.findByAgence(agenceId, statut).map(mapper::toResponse);
    }

    /**
     * Conversion d'un brouillon en Voyage publié.
     * Exige : statutBrouillon == PRET. Crée un Voyage via VoyageUseCase, puis le publie,
     * et marque le brouillon comme CONVERTI avec voyageId rempli (traçabilité).
     */
    @Override
    public Mono<VoyageDetailsDTO> publish(UUID id, UUID currentUserId) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Brouillon non trouvé : " + id)))
                .flatMap(brouillon -> {
                    if (brouillon.getStatutBrouillon() != StatutBrouillon.PRET) {
                        return Mono.error(new org.springframework.web.server.ResponseStatusException(
                            org.springframework.http.HttpStatus.BAD_REQUEST,
                            "Le brouillon doit etre au statut PRET pour etre publie (actuel : "
                                    + brouillon.getStatutBrouillon() + ")"));
                    }
                    VoyageCreateRequestDTO createDto = toVoyageCreateDTO(brouillon);
                    return voyageUseCase.createVoyage(createDto, currentUserId)
                            .flatMap(voyageDetails -> voyageUseCase
                                    .updateVoyageStatus(voyageDetails.getIdVoyage(), "PUBLIE", currentUserId))
                            .flatMap(publishedVoyage -> {
                                brouillon.setStatutBrouillon(StatutBrouillon.CONVERTI);
                                brouillon.setVoyageId(publishedVoyage.getIdVoyage());
                                return persistencePort.save(brouillon).thenReturn(publishedVoyage);
                            });
                });
    }

    // --- Helpers ---

    private void recalculerStatut(VoyageBrouillon brouillon) {
        // Ne pas écraser CONVERTI ou ANNULE
        if (brouillon.getStatutBrouillon() == StatutBrouillon.CONVERTI
                || brouillon.getStatutBrouillon() == StatutBrouillon.ANNULE) {
            return;
        }
        brouillon.setStatutBrouillon(brouillon.isComplet()
                ? StatutBrouillon.PRET
                : StatutBrouillon.INCOMPLET);
    }

    private VoyageCreateRequestDTO toVoyageCreateDTO(VoyageBrouillon b) {
        VoyageCreateRequestDTO dto = new VoyageCreateRequestDTO();
        dto.setTitre(b.getTitre());
        dto.setDescription(b.getDescription() != null ? b.getDescription() : b.getTitre());
        dto.setLieuDepart(b.getLieuDepart());
        dto.setLieuArrive(b.getLieuArrive());
        dto.setPointDeDepart(b.getPointDeDepart());
        dto.setPointArrivee(b.getPointArrivee());
        dto.setDateDepartPrev(b.getDateDepartPrev());
        dto.setHeureDepartEffectif(b.getHeureDepartEffectif());
        dto.setHeureArrive(b.getHeureArrive());
        dto.setNbrPlaceReservable(b.getNbrPlaceReservable() != null ? b.getNbrPlaceReservable() : 0);
        dto.setNbrPlaceReserve(0);
        dto.setNbrPlaceConfirm(0);
        dto.setNbrPlaceRestante(b.getNbrPlaceReservable() != null ? b.getNbrPlaceReservable() : 0);
        dto.setDateLimiteReservation(b.getDateLimiteReservation());
        dto.setDateLimiteConfirmation(b.getDateLimiteConfirmation());
        dto.setSmallImage(b.getSmallImage());
        dto.setBigImage(b.getBigImage());
        dto.setChauffeurId(b.getChauffeurId());
        dto.setVehiculeId(b.getVehiculeId());
        dto.setClassVoyageId(b.getClassVoyageId());
        dto.setAgenceVoyageId(b.getAgenceVoyageId());
        dto.setAmenities(parseAmenities(b.getAmenities()));
        return dto;
    }

    private static List<Amenities> parseAmenities(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try { return Amenities.valueOf(s); }
                    catch (IllegalArgumentException ex) { return null; }
                })
                .filter(a -> a != null)
                .collect(Collectors.toList());
    }
}