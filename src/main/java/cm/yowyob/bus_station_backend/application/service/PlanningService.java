package cm.yowyob.bus_station_backend.application.service;

import cm.yowyob.bus_station_backend.application.dto.planning.*;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageDetailsDTO;
import cm.yowyob.bus_station_backend.application.mapper.PlanningMapper;
import cm.yowyob.bus_station_backend.application.mapper.VoyageMapper;
import cm.yowyob.bus_station_backend.application.mapper.UserMapper;
import cm.yowyob.bus_station_backend.application.port.in.PlanningUseCase;
import cm.yowyob.bus_station_backend.application.port.out.*;
import cm.yowyob.bus_station_backend.domain.enums.planning.RecurrenceType;
import cm.yowyob.bus_station_backend.domain.enums.planning.StatutPlanning;
import cm.yowyob.bus_station_backend.domain.enums.StatutVoyage;
import cm.yowyob.bus_station_backend.domain.exception.BusinessRuleViolationException;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.exception.UnauthorizeException;
import cm.yowyob.bus_station_backend.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanningService implements PlanningUseCase {

    private final PlanningPersistencePort planningPort;
    private final AgencePersistencePort agencePort;
    private final VoyagePersistencePort voyagePort;
    private final UserPersistencePort userPort;
    private final NotificationPort notificationPort;
    private final PlanningMapper planningMapper;
    private final VoyageMapper voyageMapper;
    private final UserMapper userMapper;
    private final TransactionalOperator rxtx;

    // ==================== CRUD Planning ====================

    @Override
    public Mono<PlanningVoyageDTO> createPlanning(PlanningVoyageDTO dto, UUID currentUserId) {
        log.info("Création d'un planning par l'utilisateur: {}", currentUserId);

        return agencePort.findById(dto.getIdAgenceVoyage())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Agence introuvable")))
                .flatMap(agence -> {
                    // Verify the current user is the agency manager
                    if (!agence.getUserId().equals(currentUserId)) {
                        return Mono.error(new UnauthorizeException(
                                "Vous n'êtes pas autorisé à créer un planning pour cette agence"));
                    }

                    PlanningVoyage planning = planningMapper.toDomain(dto);
                    planning.setIdPlanning(UUID.randomUUID());
                    planning.setStatut(StatutPlanning.BROUILLON);
                    planning.setDateCreation(LocalDateTime.now());
                    planning.setDateModification(LocalDateTime.now());

                    return planningPort.save(planning)
                            .flatMap(savedPlanning -> {
                                // If creneaux are provided, save them too
                                if (dto.getCreneaux() != null && !dto.getCreneaux().isEmpty()) {
                                    return Flux.fromIterable(dto.getCreneaux())
                                            .map(creneauDTO -> {
                                                CreneauPlanning creneau = planningMapper.toCreneauDomain(creneauDTO);
                                                creneau.setIdCreneau(UUID.randomUUID());
                                                creneau.setIdPlanning(savedPlanning.getIdPlanning());
                                                creneau.setActif(true);
                                                return creneau;
                                            })
                                            .flatMap(planningPort::saveCreneau)
                                            .collectList()
                                            .map(creneaux -> planningMapper.toDTOWithCreneaux(savedPlanning, creneaux));
                                }
                                return Mono.just(planningMapper.toDTO(savedPlanning));
                            });
                }).as(rxtx::transactional);
    }

    @Override
    public Mono<PlanningVoyageDTO> updatePlanning(UUID planningId, PlanningVoyageDTO dto, UUID currentUserId) {
        return findPlanningAndVerifyOwnership(planningId, currentUserId)
                .flatMap(planning -> {
                    if (dto.getNom() != null) planning.setNom(dto.getNom());
                    if (dto.getDescription() != null) planning.setDescription(dto.getDescription());
                    if (dto.getRecurrence() != null) planning.setRecurrence(dto.getRecurrence());
                    if (dto.getDateDebut() != null) planning.setDateDebut(dto.getDateDebut());
                    if (dto.getDateFin() != null) planning.setDateFin(dto.getDateFin());
                    planning.setDateModification(LocalDateTime.now());

                    return planningPort.save(planning);
                })
                .flatMap(this::loadPlanningWithCreneaux);
    }

    @Override
    public Mono<Void> deletePlanning(UUID planningId, UUID currentUserId) {
        return findPlanningAndVerifyOwnership(planningId, currentUserId)
                .flatMap(planning -> planningPort.deleteCreneauxByPlanningId(planningId)
                        .then(planningPort.deleteById(planningId)));
    }

    @Override
    public Mono<PlanningVoyageDTO> getPlanningById(UUID planningId) {
        return planningPort.findById(planningId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Planning introuvable")))
                .flatMap(this::loadPlanningWithCreneaux);
    }

    @Override
    public Flux<PlanningVoyagePreviewDTO> getPlanningsByAgence(UUID agenceId) {
        return planningPort.findByAgenceId(agenceId)
                .flatMap(planning -> planningPort.findCreneauxByPlanningId(planning.getIdPlanning())
                        .collectList()
                        .flatMap(creneaux -> agencePort.findById(planning.getIdAgenceVoyage())
                                .map(agence -> planningMapper.toPreviewDTOWithAgence(
                                        planning, creneaux.size(), agence))
                                .defaultIfEmpty(planningMapper.toPreviewDTO(planning, creneaux.size()))));
    }

    // ==================== CRUD Creneaux ====================

    @Override
    public Mono<CreneauPlanningDTO> addCreneau(UUID planningId, CreneauPlanningDTO dto, UUID currentUserId) {
        return findPlanningAndVerifyOwnership(planningId, currentUserId)
                .flatMap(planning -> {
                    validateCreneauForRecurrence(dto, planning.getRecurrence());

                    CreneauPlanning creneau = planningMapper.toCreneauDomain(dto);
                    creneau.setIdCreneau(UUID.randomUUID());
                    creneau.setIdPlanning(planningId);
                    creneau.setActif(true);

                    return planningPort.saveCreneau(creneau);
                })
                .map(planningMapper::toCreneauDTO);
    }

    @Override
    public Mono<CreneauPlanningDTO> updateCreneau(UUID creneauId, CreneauPlanningDTO dto, UUID currentUserId) {
        return planningPort.findCreneauById(creneauId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Créneau introuvable")))
                .flatMap(existingCreneau -> findPlanningAndVerifyOwnership(existingCreneau.getIdPlanning(), currentUserId)
                        .flatMap(planning -> {
                            if (dto.getJourSemaine() != null) existingCreneau.setJourSemaine(dto.getJourSemaine());
                            if (dto.getJourMois() != null) existingCreneau.setJourMois(dto.getJourMois());
                            if (dto.getMois() != null) existingCreneau.setMois(dto.getMois());
                            if (dto.getTitre() != null) existingCreneau.setTitre(dto.getTitre());
                            if (dto.getDescription() != null) existingCreneau.setDescription(dto.getDescription());
                            if (dto.getHeureDepart() != null) existingCreneau.setHeureDepart(dto.getHeureDepart());
                            if (dto.getHeureArrivee() != null) existingCreneau.setHeureArrivee(dto.getHeureArrivee());
                            if (dto.getDureeEstimee() != null) existingCreneau.setDureeEstimee(dto.getDureeEstimee());
                            if (dto.getLieuDepart() != null) existingCreneau.setLieuDepart(dto.getLieuDepart());
                            if (dto.getLieuArrive() != null) existingCreneau.setLieuArrive(dto.getLieuArrive());
                            if (dto.getPointDeDepart() != null) existingCreneau.setPointDeDepart(dto.getPointDeDepart());
                            if (dto.getPointArrivee() != null) existingCreneau.setPointArrivee(dto.getPointArrivee());
                            if (dto.getIdClassVoyage() != null) existingCreneau.setIdClassVoyage(dto.getIdClassVoyage());
                            if (dto.getIdVehicule() != null) existingCreneau.setIdVehicule(dto.getIdVehicule());
                            if (dto.getIdChauffeur() != null) existingCreneau.setIdChauffeur(dto.getIdChauffeur());
                            if (dto.getNbrPlacesDisponibles() > 0) existingCreneau.setNbrPlacesDisponibles(dto.getNbrPlacesDisponibles());
                            if (dto.getDelaiReservationHeures() > 0) existingCreneau.setDelaiReservationHeures(dto.getDelaiReservationHeures());
                            if (dto.getDelaiConfirmationHeures() > 0) existingCreneau.setDelaiConfirmationHeures(dto.getDelaiConfirmationHeures());
                            if (dto.getSmallImage() != null) existingCreneau.setSmallImage(dto.getSmallImage());
                            if (dto.getBigImage() != null) existingCreneau.setBigImage(dto.getBigImage());
                            if (dto.getAmenities() != null) existingCreneau.setAmenitiesList(dto.getAmenities());
                            existingCreneau.setActif(dto.isActif());

                            return planningPort.saveCreneau(existingCreneau);
                        }))
                .map(planningMapper::toCreneauDTO);
    }

    @Override
    public Mono<Void> deleteCreneau(UUID creneauId, UUID currentUserId) {
        return planningPort.findCreneauById(creneauId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Créneau introuvable")))
                .flatMap(creneau -> findPlanningAndVerifyOwnership(creneau.getIdPlanning(), currentUserId))
                .then(planningPort.deleteCreneauById(creneauId));
    }

    @Override
    public Flux<CreneauPlanningDTO> getCreneauxByPlanning(UUID planningId) {
        return planningPort.findCreneauxByPlanningId(planningId)
                .map(planningMapper::toCreneauDTO);
    }

    // ==================== Status Management ====================

    @Override
    public Mono<PlanningVoyageDTO> activerPlanning(UUID planningId, UUID currentUserId) {
        return findPlanningAndVerifyOwnership(planningId, currentUserId)
                .flatMap(planning -> {
                    // Verify at least one active creneau exists
                    return planningPort.findCreneauxActifsByPlanningId(planningId)
                            .collectList()
                            .flatMap(creneaux -> {
                                if (creneaux.isEmpty()) {
                                    return Mono.error(new BusinessRuleViolationException(
                                            "Le planning doit avoir au moins un créneau actif pour être activé"));
                                }
                                planning.activer();
                                return planningPort.save(planning);
                            });
                })
                .flatMap(this::loadPlanningWithCreneaux);
    }

    @Override
    public Mono<PlanningVoyageDTO> desactiverPlanning(UUID planningId, UUID currentUserId) {
        return findPlanningAndVerifyOwnership(planningId, currentUserId)
                .flatMap(planning -> {
                    planning.desactiver();
                    return planningPort.save(planning);
                })
                .flatMap(this::loadPlanningWithCreneaux);
    }

    // ==================== Voyage Generation ====================

    @Override
    public Flux<VoyageDetailsDTO> generateVoyagesFromPlanning(GenerateVoyagesFromPlanningDTO dto, UUID currentUserId) {
        log.info("Génération de voyages depuis le planning {} pour la période {} - {}",
                dto.getIdPlanning(), dto.getDateDebut(), dto.getDateFin());

        return findPlanningAndVerifyOwnership(dto.getIdPlanning(), currentUserId)
                .flatMapMany(planning -> {
                    if (planning.getStatut() != StatutPlanning.ACTIF && planning.getStatut() != StatutPlanning.BROUILLON) {
                        return Flux.error(new BusinessRuleViolationException(
                                "Le planning doit être actif ou en brouillon pour générer des voyages"));
                    }

                    return planningPort.findCreneauxActifsByPlanningId(planning.getIdPlanning())
                            .collectList()
                            .flatMapMany(creneaux -> {
                                if (creneaux.isEmpty()) {
                                    return Flux.error(new BusinessRuleViolationException(
                                            "Aucun créneau actif dans ce planning"));
                                }

                                // Generate all dates matching creneaux for the given period
                                List<CreneauDatePair> pairs = new ArrayList<>();
                                LocalDate current = dto.getDateDebut();
                                LocalDate end = dto.getDateFin();

                                while (!current.isAfter(end)) {
                                    for (CreneauPlanning creneau : creneaux) {
                                        if (matchesCreneau(creneau, current, planning.getRecurrence())) {
                                            pairs.add(new CreneauDatePair(creneau, current));
                                        }
                                    }
                                    current = current.plusDays(1);
                                }

                                log.info("Nombre de voyages à générer: {}", pairs.size());
                                return Flux.fromIterable(pairs);
                            })
                            .flatMap(pair -> createVoyageFromCreneau(pair.creneau, pair.date, planning.getIdAgenceVoyage()));
                })
                .as(rxtx::transactional);
    }

    // ==================== Public Consultation ====================

    @Override
    public Flux<PlanningVoyagePreviewDTO> getPlanningsActifsByAgence(UUID agenceId) {
        return planningPort.findActifsByAgenceId(agenceId)
                .flatMap(planning -> planningPort.findCreneauxActifsByPlanningId(planning.getIdPlanning())
                        .collectList()
                        .flatMap(creneaux -> agencePort.findById(planning.getIdAgenceVoyage())
                                .map(agence -> planningMapper.toPreviewDTOWithAgence(
                                        planning, creneaux.size(), agence))
                                .defaultIfEmpty(planningMapper.toPreviewDTO(planning, creneaux.size()))));
    }

    @Override
    public Mono<PlanningVoyageDTO> getPlanningPublicById(UUID planningId) {
        return planningPort.findById(planningId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Planning introuvable")))
                .filter(p -> p.getStatut() == StatutPlanning.ACTIF)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Planning non disponible")))
                .flatMap(this::loadPlanningWithCreneaux);
    }

    // ==================== Private Helpers ====================

    private Mono<PlanningVoyage> findPlanningAndVerifyOwnership(UUID planningId, UUID currentUserId) {
        return planningPort.findById(planningId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Planning introuvable")))
                .flatMap(planning -> agencePort.findById(planning.getIdAgenceVoyage())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Agence introuvable")))
                        .flatMap(agence -> {
                            if (!agence.getUserId().equals(currentUserId)) {
                                return Mono.error(new UnauthorizeException(
                                        "Vous n'êtes pas autorisé à modifier ce planning"));
                            }
                            return Mono.just(planning);
                        }));
    }

    private Mono<PlanningVoyageDTO> loadPlanningWithCreneaux(PlanningVoyage planning) {
        return planningPort.findCreneauxByPlanningId(planning.getIdPlanning())
                .collectList()
                .map(creneaux -> planningMapper.toDTOWithCreneaux(planning, creneaux));
    }

    private void validateCreneauForRecurrence(CreneauPlanningDTO dto, RecurrenceType recurrence) {
        switch (recurrence) {
            case HEBDOMADAIRE:
                if (dto.getJourSemaine() == null) {
                    throw new BusinessRuleViolationException(
                            "Le jour de la semaine est obligatoire pour un planning hebdomadaire");
                }
                break;
            case MENSUEL:
                if (dto.getJourMois() == null || dto.getJourMois() < 1 || dto.getJourMois() > 31) {
                    throw new BusinessRuleViolationException(
                            "Le jour du mois (1-31) est obligatoire pour un planning mensuel");
                }
                break;
            case ANNUEL:
                if (dto.getJourMois() == null || dto.getMois() == null) {
                    throw new BusinessRuleViolationException(
                            "Le jour du mois et le mois sont obligatoires pour un planning annuel");
                }
                break;
            case QUOTIDIEN:
                // No special validation needed for daily
                break;
        }
    }

    /**
     * Checks if a creneau matches a given date based on the recurrence type.
     */
    private boolean matchesCreneau(CreneauPlanning creneau, LocalDate date, RecurrenceType recurrence) {
        return switch (recurrence) {
            case QUOTIDIEN -> true; // Every day
            case HEBDOMADAIRE -> creneau.getJourSemaine() != null
                    && date.getDayOfWeek() == creneau.getJourSemaine();
            case MENSUEL -> creneau.getJourMois() != null
                    && date.getDayOfMonth() == creneau.getJourMois();
            case ANNUEL -> creneau.getJourMois() != null
                    && creneau.getMois() != null
                    && date.getDayOfMonth() == creneau.getJourMois()
                    && date.getMonthValue() == creneau.getMois();
        };
    }

    /**
     * Creates a Voyage and its LigneVoyage from a planning creneau for a given date.
     */
    private Mono<VoyageDetailsDTO> createVoyageFromCreneau(CreneauPlanning creneau, LocalDate date, UUID agenceId) {
        LocalDateTime dateDepart = LocalDateTime.of(date, creneau.getHeureDepart());
        LocalDateTime heureArrivee = creneau.getHeureArrivee() != null
                ? LocalDateTime.of(date, creneau.getHeureArrivee())
                : null;

        // If arrival is before departure, it means arrival is next day
        if (heureArrivee != null && heureArrivee.isBefore(dateDepart)) {
            heureArrivee = heureArrivee.plusDays(1);
        }

        Duration duree = creneau.getDureeEstimee();
        if (duree == null && heureArrivee != null) {
            duree = Duration.between(dateDepart, heureArrivee);
        }

        LocalDateTime dateLimiteReservation = dateDepart.minusHours(
                creneau.getDelaiReservationHeures() > 0 ? creneau.getDelaiReservationHeures() : 2);
        LocalDateTime dateLimiteConfirmation = dateDepart.minusHours(
                creneau.getDelaiConfirmationHeures() > 0 ? creneau.getDelaiConfirmationHeures() : 1);

        Voyage voyage = Voyage.builder()
                .idVoyage(UUID.randomUUID())
                .titre(creneau.getTitre() != null ? creneau.getTitre()
                        : creneau.getLieuDepart() + " → " + creneau.getLieuArrive())
                .description(creneau.getDescription())
                .dateDepartPrev(dateDepart)
                .lieuDepart(creneau.getLieuDepart())
                .lieuArrive(creneau.getLieuArrive())
                .pointDeDepart(creneau.getPointDeDepart())
                .pointArrivee(creneau.getPointArrivee())
                .heureArrive(heureArrivee)
                .dureeVoyage(duree)
                .nbrPlaceReservable(creneau.getNbrPlacesDisponibles())
                .nbrPlaceReserve(0)
                .nbrPlaceConfirm(0)
                .nbrPlaceRestante(creneau.getNbrPlacesDisponibles())
                .datePublication(LocalDateTime.now())
                .dateLimiteReservation(dateLimiteReservation)
                .dateLimiteConfirmation(dateLimiteConfirmation)
                .statusVoyage(StatutVoyage.EN_ATTENTE)
                .smallImage(creneau.getSmallImage() != null ? creneau.getSmallImage() : "default_small.jpg")
                .bigImage(creneau.getBigImage() != null ? creneau.getBigImage() : "default_big.jpg")
                .build();

        if (creneau.getAmenities() != null) {
            voyage.setAmenities(creneau.getAmenitiesList());
        }

        LigneVoyage ligneVoyage = LigneVoyage.builder()
                .idLigneVoyage(UUID.randomUUID())
                .idVoyage(voyage.getIdVoyage())
                .idAgenceVoyage(agenceId)
                .idClassVoyage(creneau.getIdClassVoyage())
                .idVehicule(creneau.getIdVehicule())
                .idChauffeur(creneau.getIdChauffeur())
                .build();

        return voyagePort.save(voyage)
                .then(voyagePort.saveLigneVoyage(ligneVoyage))
                .thenReturn(voyageMapper.toDetailsDTO(voyage));
    }

    /**
     * Helper record to pair a creneau with a specific date for voyage generation.
     */
    private record CreneauDatePair(CreneauPlanning creneau, LocalDate date) {}
}
