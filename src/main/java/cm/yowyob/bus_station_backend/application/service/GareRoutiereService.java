// src/main/java/cm/yowyob/bus_station_backend/application/service/GareRoutiereService.java

package cm.yowyob.bus_station_backend.application.service;

import cm.yowyob.bus_station_backend.application.dto.bsm.BsmStatistiquesDTO;
import cm.yowyob.bus_station_backend.application.dto.gareRoutiere.GareRoutiereDTO;
import cm.yowyob.bus_station_backend.application.dto.gareRoutiere.GareRoutiereUpdateDTO;
import cm.yowyob.bus_station_backend.application.mapper.GareRoutiereMapper;
import cm.yowyob.bus_station_backend.application.port.out.AgencePersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.CoordonneePersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.GareRoutierePersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.UserPersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.VoyagePersistencePort;
import cm.yowyob.bus_station_backend.domain.enums.ServicesGareRoutiere;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.model.AgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.GareRoutiere;
import cm.yowyob.bus_station_backend.domain.model.Voyage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class GareRoutiereService {

    private final UserPersistencePort userPersistencePort;
    private final GareRoutierePersistencePort gareRoutierePersistencePort;
    private final AgencePersistencePort agencePersistencePort;
    private final CoordonneePersistencePort coordonneePersistencePort;
    private final VoyagePersistencePort voyagePersistencePort;
    private final GareRoutiereMapper gareRoutiereMapper;

    // ============================================================
    // EXISTANT (LOT 0..7)
    // ============================================================

    public Mono<GareRoutiere> createGareRoutiere(GareRoutiere gareRoutiere) {
        gareRoutiere.setIdGareRoutiere(UUID.randomUUID());
        return this.userPersistencePort.findById(gareRoutiere.getManagerId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Gestionnaire avec l'id " + gareRoutiere.getManagerId() + " non trouvé")))
                .flatMap(user -> this.gareRoutierePersistencePort.saveGareRoutiere(gareRoutiere));
    }

    public Mono<GareRoutiereDTO> getGareRoutiereByManagerId(UUID managerId) {
        return this.gareRoutierePersistencePort.getGareRoutiereByManagerId(managerId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Aucune gare trouvée pour le manager " + managerId)))
                .flatMap(gareRoutiere -> {
                    if (gareRoutiere.getIdCoordonneeGPS() == null) {
                        return Mono.just(gareRoutiereMapper.toDTO(gareRoutiere, null));
                    }
                    return this.coordonneePersistencePort.findById(gareRoutiere.getIdCoordonneeGPS())
                            .map(coordonnee -> gareRoutiereMapper.toDTO(gareRoutiere, coordonnee))
                            .switchIfEmpty(Mono.fromCallable(() -> gareRoutiereMapper.toDTO(gareRoutiere, null)));
                });
    }

    public Mono<Page<GareRoutiere>> getAllGaresRoutieres(String searchTerm, List<ServicesGareRoutiere> services,
                                                        Pageable pageable) {
        return this.gareRoutierePersistencePort.findAll(searchTerm, services, pageable);
    }

    public Mono<GareRoutiereDTO> getGareRoutiereById(UUID gareRoutiereId) {
        if (gareRoutiereId == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'ID de la gare routière est requis"));
        }
        return this.gareRoutierePersistencePort.getGareRoutiereById(gareRoutiereId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Gare routière avec l'id " + gareRoutiereId + " non trouvée")))
                .flatMap(gareRoutiere -> {
                    if (gareRoutiere.getIdCoordonneeGPS() == null) {
                        return Mono.just(gareRoutiereMapper.toDTO(gareRoutiere, null));
                    }
                    return this.coordonneePersistencePort.findById(gareRoutiere.getIdCoordonneeGPS())
                            .map(coordonnee -> gareRoutiereMapper.toDTO(gareRoutiere, coordonnee))
                            .switchIfEmpty(Mono.fromCallable(() -> gareRoutiereMapper.toDTO(gareRoutiere, null)));
                });
    }

    // ============================================================
    // LOT 8 — Gare & BSM
    // ============================================================

    /**
     * PUT /gare/{gareId} — modification BSM.
     * Vérifie que le user connecté est le manager de la gare avant d'appliquer.
     * Champs PATCH-like : null = ignoré.
     */
    public Mono<GareRoutiereDTO> updateGareRoutiere(UUID gareId, GareRoutiereUpdateDTO dto, UUID currentUserId) {
        if (gareId == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'ID de la gare routière est requis"));
        }
        return gareRoutierePersistencePort.getGareRoutiereById(gareId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Gare routière avec l'id " + gareId + " non trouvée")))
                .flatMap(existing -> {
                    // Vérifier que le user connecté est bien le manager de cette gare
                    if (existing.getManagerId() == null || !existing.getManagerId().equals(currentUserId)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Vous n'êtes pas le manager de cette gare"));
                    }

                    // Appliquer les champs non-null (PATCH-like)
                    if (dto.getNomGareRoutiere() != null) existing.setNomGareRoutiere(dto.getNomGareRoutiere());
                    if (dto.getAdresse() != null) existing.setAdresse(dto.getAdresse());
                    if (dto.getVille() != null) existing.setVille(dto.getVille());
                    if (dto.getQuartier() != null) existing.setQuartier(dto.getQuartier());
                    if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
                    if (dto.getServices() != null) existing.setServices(dto.getServices());
                    if (dto.getHoraires() != null) existing.setHoraires(dto.getHoraires());
                    if (dto.getPhotoUrl() != null) existing.setPhotoUrl(dto.getPhotoUrl());
                    if (dto.getNomPresident() != null) existing.setNomPresident(dto.getNomPresident());

                    return gareRoutierePersistencePort.updateGareRoutiere(existing);
                })
                .flatMap(saved -> {
                    if (saved.getIdCoordonneeGPS() == null) {
                        return Mono.just(gareRoutiereMapper.toDTO(saved, null));
                    }
                    return coordonneePersistencePort.findById(saved.getIdCoordonneeGPS())
                            .map(coordonnee -> gareRoutiereMapper.toDTO(saved, coordonnee))
                            .switchIfEmpty(Mono.fromCallable(() -> gareRoutiereMapper.toDTO(saved, null)));
                });
    }

    /**
     * GET /gare/{gareId}/agences — liste des agences affiliées à la gare.
     */
    public Flux<AgenceVoyage> getAgencesByGareId(UUID gareId) {
        if (gareId == null) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'ID de la gare routière est requis"));
        }
        return gareRoutierePersistencePort.getGareRoutiereById(gareId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Gare routière avec l'id " + gareId + " non trouvée")))
                .flatMapMany(gare -> agencePersistencePort.findByGareRoutiereId(gareId));
    }

    /**
     * GET /gare/{gareId}/voyages?date=YYYY-MM-DD — voyages d'une gare.
     * Si date == null : tous les voyages de la gare paginés.
     */
    public Mono<Page<Voyage>> getVoyagesByGareId(UUID gareId, LocalDate date, Pageable pageable) {
        if (gareId == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'ID de la gare routière est requis"));
        }
        return gareRoutierePersistencePort.getGareRoutiereById(gareId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Gare routière avec l'id " + gareId + " non trouvée")))
                .flatMap(gare -> {
                    Mono<List<Voyage>> contentMono = voyagePersistencePort
                            .findByGareRoutiereIdAndDate(gareId, date, pageable)
                            .collectList();
                    Mono<Long> countMono = voyagePersistencePort
                            .countByGareRoutiereIdAndDate(gareId, date);
                    return Mono.zip(contentMono, countMono)
                            .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
                });
    }

    /**
     * GET /bsm/statistiques/{gareId} — KPIs dashboard BSM.
     */
    public Mono<BsmStatistiquesDTO> getStatistiques(UUID gareId) {
        if (gareId == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'ID de la gare routière est requis"));
        }
        return gareRoutierePersistencePort.getGareRoutiereById(gareId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Gare routière avec l'id " + gareId + " non trouvée")))
                .flatMap(gare -> {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDate today = LocalDate.now();

                    Mono<Long> agencesAffiliees = agencePersistencePort.findByGareRoutiereId(gareId)
                            .count();
                    Mono<Long> agencesActives = agencePersistencePort.findByGareRoutiereId(gareId)
                            .filter(a -> Boolean.TRUE.equals(a.getIsActive()))
                            .count();
                    Mono<Long> voyagesAujourdhui = voyagePersistencePort
                            .countVoyagesByGareIdAndDate(gareId, today);
                    Mono<Long> voyagesAVenir = voyagePersistencePort
                            .countPublicVoyagesByGareIdAfter(gareId, now);
                    Mono<Double> tauxMoyen = voyagePersistencePort
                            .avgTauxRemplissageByGareId(gareId);

                    return Mono.zip(agencesAffiliees, agencesActives, voyagesAujourdhui, voyagesAVenir, tauxMoyen)
                            .map(t -> BsmStatistiquesDTO.builder()
                                    .gareId(gareId)
                                    .nbAgencesAffiliees(t.getT1())
                                    .nbAgencesActives(t.getT2())
                                    .nbVoyagesAujourdhui(t.getT3())
                                    .nbVoyagesAVenir(t.getT4())
                                    .tauxRemplissageMoyen(t.getT5())
                                    .build());
                });
    }
}