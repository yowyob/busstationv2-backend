package cm.yowyob.bus_station_backend.application.service;

import cm.yowyob.bus_station_backend.application.dto.classVoyage.ClassVoyageDTO;
import cm.yowyob.bus_station_backend.application.dto.classVoyage.ClassVoyageResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageCreateRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageDetailsDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyagePreviewDTO;
import cm.yowyob.bus_station_backend.application.mapper.UserMapper;
import cm.yowyob.bus_station_backend.application.mapper.VoyageMapper;
import cm.yowyob.bus_station_backend.application.port.in.VoyageUseCase;
import cm.yowyob.bus_station_backend.application.port.out.AgencePersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.GareRoutierePersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.NotificationPort;
import cm.yowyob.bus_station_backend.application.port.out.UserPersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.VoyagePersistencePort;
import cm.yowyob.bus_station_backend.domain.enums.StatutVoyage;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.factory.NotificationFactory;
import cm.yowyob.bus_station_backend.domain.model.ClassVoyage;
import cm.yowyob.bus_station_backend.domain.model.LigneVoyage;
import cm.yowyob.bus_station_backend.domain.model.Voyage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class VoyageService implements VoyageUseCase {

    private final VoyagePersistencePort voyagePersistencePort;
    private final AgencePersistencePort agencePersistencePort;
    private final GareRoutierePersistencePort gareRoutierePersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final NotificationPort notificationPort;
    private final VoyageMapper voyageMapper;
    private final TransactionalOperator rxtx;
    private final UserMapper userMapper;

    public VoyageService(VoyagePersistencePort voyagePersistencePort,
                         AgencePersistencePort agencePersistencePort,
                         GareRoutierePersistencePort gareRoutierePersistencePort,
                         UserPersistencePort userPersistencePort,
                         NotificationPort notificationPort,
                         VoyageMapper voyageMapper,
                         TransactionalOperator rxtx,
                         UserMapper userMapper) {
        this.voyagePersistencePort = voyagePersistencePort;
        this.agencePersistencePort = agencePersistencePort;
        this.gareRoutierePersistencePort = gareRoutierePersistencePort;
        this.userPersistencePort = userPersistencePort;
        this.notificationPort = notificationPort;
        this.voyageMapper = voyageMapper;
        this.rxtx = rxtx;
        this.userMapper = userMapper;
    }

    @Override
    public Mono<VoyageDetailsDTO> createVoyage(VoyageCreateRequestDTO dto, UUID currentUserId) {
        LocalDateTime now = LocalDateTime.now();
        log.info("Création d'un voyage par l'utilisateur: {}", currentUserId);

        return Mono.zip(
                agencePersistencePort.findById(dto.getAgenceVoyageId())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Agence introuvable"))),
                voyagePersistencePort.findClassVoyageById(dto.getClassVoyageId())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Classe voyage introuvable"))),
                agencePersistencePort.findVehiculeById(dto.getVehiculeId())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Véhicule introuvable"))),
                userPersistencePort.findChauffeurByUserId(dto.getChauffeurId())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Chauffeur introuvable"))))
                .flatMap(tuple -> {
                    var agence = tuple.getT1();
                    var classVoyage = tuple.getT2();
                    var vehicule = tuple.getT3();
                    var chauffeur = tuple.getT4();

                    // Logique métier : Vérifier que le chauffeur appartient à l'agence
                    if (!chauffeur.getAgenceVoyageId().equals(dto.getAgenceVoyageId())) {
                        return Mono.error(new IllegalArgumentException("Le chauffeur n'appartient pas à cette agence"));
                    }

                    Voyage voyage = voyageMapper.toDomain(dto);
                    voyage.setIdVoyage(UUID.randomUUID());
                    voyage.setDatePublication(LocalDateTime.now());

                    // Calcul de durée (comme dans l'ancien backend)
                    if (dto.getDateDepartPrev() != null && dto.getHeureArrive() != null) {
                        Duration duree = Duration.between(dto.getDateDepartPrev(), dto.getHeureArrive());
                        voyage.setDureeVoyage(duree);
                    }

                    // Images par défaut
                    voyage.setSmallImage(dto.getSmallImage() != null ? dto.getSmallImage() : "default_small.jpg");
                    voyage.setBigImage(dto.getBigImage() != null ? dto.getBigImage() : "default_big.jpg");

                    LigneVoyage ligneVoyage = LigneVoyage.builder()
                            .idLigneVoyage(UUID.randomUUID())
                            .idVoyage(voyage.getIdVoyage())
                            .idAgenceVoyage(dto.getAgenceVoyageId())
                            .idClassVoyage(dto.getClassVoyageId())
                            .idVehicule(dto.getVehiculeId())
                            .idChauffeur(chauffeur.getChauffeurId())
                            .build();

                    return voyagePersistencePort.save(voyage)
                            .then(voyagePersistencePort.saveLigneVoyage(ligneVoyage))
                            .then(notificationPort.sendNotification(
                                    NotificationFactory.createVoyageCreatedEvent(voyage, agence.getUserId())))
                            .then(notificationPort.sendNotification(
                                    NotificationFactory.createDriverAssignedEvent(chauffeur.getUserId(), voyage)))
                            .thenReturn(voyageMapper.toDetailsDTO(voyage))
                            .flatMap(details -> enrichVoyageDetails(details, voyage.getIdVoyage()));
                }).as(rxtx::transactional);
    }

    @Override
    public Mono<VoyageDetailsDTO> getVoyageById(UUID voyageId) {
        return voyagePersistencePort.findById(voyageId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Voyage introuvable")))
                .map(voyageMapper::toDetailsDTO)
                .flatMap(details -> enrichVoyageDetails(details, voyageId));
    }

    @Override
    public Mono<Page<VoyagePreviewDTO>> getAllVoyagesPreview(Pageable pageable) {
        return voyagePersistencePort.findAll(pageable)
                .flatMap(v -> voyagePersistencePort.findLigneVoyageByVoyageId(v.getIdVoyage())
                        .flatMap(ligne -> Mono.zip(
                                agencePersistencePort.findById(ligne.getIdAgenceVoyage()),
                                voyagePersistencePort.findClassVoyageById(ligne.getIdClassVoyage())).map(t -> {
                                    VoyagePreviewDTO preview = voyageMapper.toPreviewDTO(v);
                                    return voyageMapper.enrichPreviewDTO(preview, t.getT1(), t.getT2());
                                })))
                .collectList()
                .zipWith(voyagePersistencePort.countVoyagesByAgenceId(null)) // null pour total general
                .map(t -> (Page<VoyagePreviewDTO>) new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }

    @Override
    public Mono<Page<VoyagePreviewDTO>> getVoyagesByAgence(UUID agenceId, Pageable pageable) {
        return voyagePersistencePort.findByAgenceId(agenceId, pageable)
                .flatMap(v -> voyagePersistencePort.findLigneVoyageByVoyageId(v.getIdVoyage())
                        .flatMap(ligne -> Mono.zip(
                                agencePersistencePort.findById(ligne.getIdAgenceVoyage()),
                                voyagePersistencePort.findClassVoyageById(ligne.getIdClassVoyage())).map(t -> {
                                    VoyagePreviewDTO preview = voyageMapper.toPreviewDTO(v);
                                    return voyageMapper.enrichPreviewDTO(preview, t.getT1(), t.getT2());
                                })))
                .collectList()
                .zipWith(voyagePersistencePort.countVoyagesByAgenceId(agenceId))
                .map(t -> (Page<VoyagePreviewDTO>) new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }

    @Override
    public Mono<Page<VoyagePreviewDTO>> getVoyagesByGareRoutiere(UUID gareId, Pageable pageable) {
        log.info("Appel de getVoyagesByGareRoutiere pour gareId: {}", gareId);
        return gareRoutierePersistencePort.getGareRoutiereById(gareId)
                .flatMap(gare -> {
                    String nomGare = gare.getNomGareRoutiere();
                    log.info("Gare trouvée: {}", nomGare);
                    return voyagePersistencePort.findByPointName(nomGare, pageable)
                            .flatMap(v -> {
                                VoyagePreviewDTO preview = voyageMapper.toPreviewDTO(v);
                                return Mono.just(preview);
                            })
                            .collectList()
                            .zipWith(voyagePersistencePort.countVoyagesByPointName(nomGare))
                            .map(t -> (Page<VoyagePreviewDTO>) new PageImpl<>(t.getT1(), pageable, t.getT2()));
                })
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Gare routière introuvable")))
                .doOnError(e -> log.error("Erreur détectée dans getVoyagesByGareRoutiere: ", e));
    }

    @Override
    public Mono<VoyageDetailsDTO> updateVoyageStatus(UUID voyageId, String newStatus, UUID currentUserId) {
        return voyagePersistencePort.findById(voyageId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Voyage introuvable")))
                .flatMap(v -> {
                    v.setStatusVoyage(StatutVoyage.valueOf(newStatus));
                    return voyagePersistencePort.save(v);
                })
                .map(voyageMapper::toDetailsDTO)
                .flatMap(details -> enrichVoyageDetails(details, voyageId));
    }

    @Override
    public Mono<VoyageDetailsDTO> assignChauffeurAndVehicule(UUID voyageId, UUID chauffeurId, UUID vehiculeId,
            UUID currentUserId) {
        return voyagePersistencePort.findLigneVoyageByVoyageId(voyageId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Ligne de voyage introuvable")))
                .flatMap(ligne -> {
                    ligne.setIdChauffeur(chauffeurId);
                    ligne.setIdVehicule(vehiculeId);
                    return voyagePersistencePort.saveLigneVoyage(ligne);
                })
                .then(getVoyageById(voyageId));
    }

    @Override
    public Flux<Integer> getOccupiedPlaces(UUID voyageId) {
        return voyagePersistencePort.findOccupiedPlacesByVoyageId(voyageId);
    }

    // --- Gestion des Classes de Voyage ---

    @Override
    public Mono<ClassVoyageDTO> createClassVoyage(ClassVoyageDTO dto) {
        ClassVoyage cv = ClassVoyage.builder()
                .idClassVoyage(UUID.randomUUID())
                .nom(dto.getNom())
                .prix(dto.getPrix())
                .idAgenceVoyage(dto.getIdAgenceVoyage())
                .build();
        return voyagePersistencePort.saveClassVoyage(cv)
                .map(voyageMapper::mapToClassVoyageDTO);
    }

    @Override
    public Mono<ClassVoyageDTO> getClassVoyageById(UUID id) {
        return voyagePersistencePort.findClassVoyageById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Classe voyage introuvable")))
                .map(voyageMapper::mapToClassVoyageDTO);
    }

    @Override
    public Flux<ClassVoyageResponseDTO> getClassVoyagesByAgence(UUID agenceId) {
        return voyagePersistencePort.findClassVoyagesByAgence(agenceId).map(voyageMapper::mapToClassVoyageResponseDTO);
    }

    // --- Helpers Privés ---

    /**
     * Agrège les données pour construire un VoyageDetailsDTO complet
     */
    private Mono<VoyageDetailsDTO> enrichVoyageDetails(
            VoyageDetailsDTO details,
            UUID voyageId) {
        return voyagePersistencePort.findLigneVoyageByVoyageId(voyageId)
                .flatMap(ligne -> Mono.zip(
                        agencePersistencePort.findById(ligne.getIdAgenceVoyage()),
                        voyagePersistencePort.findClassVoyageById(ligne.getIdClassVoyage()),
                        agencePersistencePort.findVehiculeById(ligne.getIdVehicule()),
                        userPersistencePort.findChauffeurById(ligne.getIdChauffeur())
                                .flatMap(chauffeur -> userPersistencePort.findById(chauffeur.getUserId())),
                        getOccupiedPlaces(voyageId).collectList()))
                .map(tuple -> {
                    var agence = tuple.getT1();
                    var classVoyage = tuple.getT2();
                    var vehicule = tuple.getT3();
                    var chauffeurUser = tuple.getT4();
                    var places = tuple.getT5();

                    return voyageMapper.enrichDetailsDTOWithMappedEntities(
                            details,
                            agence != null ? agence.getLongName() : null,
                            classVoyage != null ? classVoyage.getNom() : null,
                            classVoyage != null ? classVoyage.getPrix() : 0d,
                            vehicule,
                            chauffeurUser != null
                                    ? userMapper.toResponseDTO(chauffeurUser)
                                    : null,
                            places);
                });
    }

    // --- Implémentations CRUD de base manquantes ---

    @Override
    public Mono<VoyageDetailsDTO> updateVoyage(UUID voyageId, VoyageDTO dto, UUID currentUserId) {
        return voyagePersistencePort.findById(voyageId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Voyage introuvable")))
                .flatMap(voyage -> {
                    // Mise à jour des champs modifiables
                    voyage.setTitre(dto.getTitre());
                    voyage.setDescription(dto.getDescription());
                    voyage.setDateDepartPrev(dto.getDateDepartPrev());
                    voyage.setLieuDepart(dto.getLieuDepart());
                    voyage.setLieuArrive(dto.getLieuArrive());
                    voyage.setHeureDepartEffectif(dto.getHeureDepartEffectif());
                    voyage.setHeureArrive(dto.getHeureArrive());
                    voyage.setDateLimiteReservation(dto.getDateLimiteReservation());
                    voyage.setDateLimiteConfirmation(dto.getDateLimiteConfirmation());
                    voyage.setStatusVoyage(dto.getStatusVoyage());
                    voyage.setSmallImage(dto.getSmallImage());
                    voyage.setBigImage(dto.getBigImage());
                    voyage.setAmenities(dto.getAmenities());

                    return voyagePersistencePort.save(voyage);
                })
                .map(voyageMapper::toDetailsDTO)
                .flatMap(details -> enrichVoyageDetails(details, voyageId));
    }

    @Override
    public Mono<Void> deleteVoyage(UUID voyageId, UUID currentUserId) {
        return voyagePersistencePort.findById(voyageId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Voyage introuvable")))
                .then(voyagePersistencePort.deleteById(voyageId));
    }

    @Override
    public Mono<Page<VoyagePreviewDTO>> getAllVoyages(Pageable pageable) {
        return getAllVoyagesPreview(pageable);
    }

    @Override
    public Mono<ClassVoyageDTO> updateClassVoyage(UUID classVoyageId, ClassVoyageDTO dto) {
        return voyagePersistencePort.findClassVoyageById(classVoyageId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Classe voyage introuvable")))
                .flatMap(classVoyage -> {
                    classVoyage.setNom(dto.getNom());
                    classVoyage.setPrix(dto.getPrix());
                    classVoyage.setIdAgenceVoyage(dto.getIdAgenceVoyage());
                    return voyagePersistencePort.saveClassVoyage(classVoyage);
                })
                .map(voyageMapper::mapToClassVoyageDTO);
    }

    @Override
    public Mono<Void> deleteClassVoyage(UUID id) {
        return voyagePersistencePort.deleteClassVoyageById(id);
    }

    @Override
    public Mono<Page<ClassVoyageDTO>> getAllClassVoyages(Pageable pageable) {
        return voyagePersistencePort.findAllClassVoyages(pageable)
                .map(voyageMapper::mapToClassVoyageDTO)
                .collectList()
                .zipWith(voyagePersistencePort.countVoyagesByAgenceId(null))
                .map(t -> (Page<ClassVoyageDTO>) new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }

    @Override
    public Flux<VoyagePreviewDTO> getVoyagesSimilaires(UUID voyageId, int limit) {
        return voyagePersistencePort.findById(voyageId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Voyage non trouvé")))
                .flatMapMany(voyage -> voyagePersistencePort.findLigneVoyageByVoyageId(voyageId)
                        .flatMapMany(ligne -> voyagePersistencePort.findSimilaires(
                                voyageId,
                                voyage.getLieuDepart(),
                                voyage.getLieuArrive(),
                                ligne.getIdAgenceVoyage(),
                                LocalDateTime.now(),
                                limit)))
                .map(voyageMapper::toPreviewDTO);
    }

    @Override
    public Mono<Page<VoyagePreviewDTO>> getVoyagesPublicsByAgence(UUID agenceId, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return voyagePersistencePort.findPublicByAgenceId(agenceId, now, pageable)
                .map(voyageMapper::toPreviewDTO)
                .collectList()
                .zipWith(voyagePersistencePort.countPublicByAgenceId(agenceId, now))
                .map(tuple -> new org.springframework.data.domain.PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    @Override
    public Mono<Page<VoyagePreviewDTO>> searchVoyages(String lieuDepart, String lieuArrive, String date, UUID classId, UUID agenceId, Pageable pageable) {
        return voyagePersistencePort.searchVoyages(lieuDepart, lieuArrive, date, classId, agenceId, pageable)
                .map(voyageMapper::toPreviewDTO)
                .collectList()
                .zipWith(voyagePersistencePort.countSearchVoyages(lieuDepart, lieuArrive, date, classId, agenceId))
                .map(tuple -> new org.springframework.data.domain.PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }
}
