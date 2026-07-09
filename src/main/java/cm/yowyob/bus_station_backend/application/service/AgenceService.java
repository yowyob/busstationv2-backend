package cm.yowyob.bus_station_backend.application.service;

import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageDTO;
import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyagePreviewDTO;
import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.vehicule.VehiculeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import cm.yowyob.bus_station_backend.application.mapper.AgenceVoyageMapper;
import cm.yowyob.bus_station_backend.application.mapper.VehiculeMapper;
import cm.yowyob.bus_station_backend.application.port.in.AgenceUseCase;
import cm.yowyob.bus_station_backend.application.port.out.AgencePersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.GareRoutierePersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.NotificationPort;
import cm.yowyob.bus_station_backend.application.port.out.OrganizationPersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.UserPersistencePort;
import cm.yowyob.bus_station_backend.domain.enums.RoleType;
import cm.yowyob.bus_station_backend.domain.exception.BusinessRuleViolationException;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.exception.UnauthorizeException;
import cm.yowyob.bus_station_backend.domain.factory.NotificationFactory;
import cm.yowyob.bus_station_backend.domain.model.AgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.PolitiqueAnnulation;
import cm.yowyob.bus_station_backend.domain.model.Vehicule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgenceService implements AgenceUseCase {

    private final AgencePersistencePort agencePort;
    private final NotificationPort notificationPort;
    private final UserPersistencePort userPersistencePort;
    private final OrganizationPersistencePort organizationPersistencePort;
    private final GareRoutierePersistencePort gareRoutierePersistencePort;
    private final AgenceVoyageMapper agenceMapper;
    private final VehiculeMapper vehiculeMapper;
    private final TransactionalOperator rxtx;

    @Override
    public Mono<AgenceVoyageResponseDTO> getAgenceById(UUID id) {
        return agencePort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Agence non trouvée")))
                .map(agenceMapper::toResponseDTO);
    }

    @Override
    public Mono<AgenceVoyageResponseDTO> getAgenceByChefAgenceId(UUID chefId) {
        return agencePort.findByChefAgenceId(chefId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Aucune agence pour ce chef")))
                .map(agenceMapper::toResponseDTO);
    }

    @Override
    public Mono<Page<AgenceVoyageResponseDTO>> getAllAgences(Pageable pageable) {
        return agencePort.findAll(pageable)
                .map(page -> page.map(agenceMapper::toResponseDTO));
    }

    @Override
    public Mono<AgenceVoyageResponseDTO> createAgence(AgenceVoyageDTO agenceDTO) {
        return userPersistencePort.findById(agenceDTO.getUser_id())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Utilisateur non trouvé")))
                .flatMap(user -> organizationPersistencePort.findByOrganisationId(agenceDTO.getOrganisation_id())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Organisation non trouvée")))
                        .flatMap(org -> gareRoutierePersistencePort.getGareRoutiereById(agenceDTO.getGare_routiere_id())
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Gare routière non trouvée")))
                                .flatMap(gare -> Mono.zip(
                                        agencePort.existsByLongName(agenceDTO.getLong_name()),
                                        agencePort.existsByShortName(agenceDTO.getShort_name())).flatMap(tuple -> {
                                            if (tuple.getT1() || tuple.getT2()) {
                                                return Mono.error(
                                                        new BusinessRuleViolationException("Cette agence existe déjà"));
                                            }

                                            AgenceVoyage agence = AgenceVoyage.builder()
                                                    .organisationId(agenceDTO.getOrganisation_id())
                                                    .userId(user.getUserId())
                                                    .longName(agenceDTO.getLong_name())
                                                    .shortName(agenceDTO.getShort_name())
                                                    .location(agenceDTO.getLocation())
                                                    .gareRoutiereId(agenceDTO.getGare_routiere_id())
                                                    .socialNetwork(agenceDTO.getSocial_network())
                                                    .description(agenceDTO.getDescription())
                                                    .greetingMessage(agenceDTO.getGreeting_message())
                                                    .isActive(false)
                                                    .build();

                                            // Mise à jour des rôles de l'utilisateur
                                            if (!user.getRoles().contains(RoleType.AGENCE_VOYAGE)) {
                                                List<RoleType> roles = new ArrayList<>(user.getRoles());
                                                roles.add(RoleType.AGENCE_VOYAGE);
                                                user.setRoles(roles);
                                            }

                                            return agencePort.save(agence)
                                                    .flatMap(savedAgence -> Mono.when(
                                                            userPersistencePort.save(user),
                                                            notificationPort.sendNotification(
                                                                    NotificationFactory.createAgencyCreatedEvent(
                                                                            savedAgence,
                                                                            user)))
                                                            .thenReturn(savedAgence))
                                                    .map(agenceMapper::toResponseDTO);
                                }))))
                .as(rxtx::transactional);
    }

    @Override
    @Transactional
    public Mono<AgenceVoyageDTO> updateAgence(UUID id, AgenceVoyageDTO dto, UUID currentUserId) {
        return agencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Agence non trouvée")))
                .filter(agence -> agence.getUserId().equals(currentUserId))
                .switchIfEmpty(Mono.error(
                        new UnauthorizeException(
                                "Vous n'êtes pas le chef de cette agence")))
                .flatMap(existing -> {

                    if (dto.getGare_routiere_id() != null) {
                        return gareRoutierePersistencePort.getGareRoutiereById(dto.getGare_routiere_id())
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Gare routière non trouvée")))
                                .map(gare -> {
                                    existing.setGareRoutiereId(dto.getGare_routiere_id());
                                    return existing;
                                });
                    }
                    return Mono.just(existing);
                })
                .map(existing -> {

                    if (dto.getOrganisation_id() != null) {
                        existing.setOrganisationId(dto.getOrganisation_id());
                    }

                    if (dto.getUser_id() != null) {
                        existing.setUserId(dto.getUser_id());
                    }

                    if (dto.getLong_name() != null) {
                        existing.setLongName(dto.getLong_name());
                    }

                    if (dto.getShort_name() != null) {
                        existing.setShortName(dto.getShort_name());
                    }

                    if (dto.getLocation() != null) {
                        existing.setLocation(dto.getLocation());
                    }

                    if (dto.getSocial_network() != null) {
                        existing.setSocialNetwork(dto.getSocial_network());
                    }

                    if (dto.getDescription() != null) {
                        existing.setDescription(dto.getDescription());
                    }

                    if (dto.getGreeting_message() != null) {
                        existing.setGreetingMessage(dto.getGreeting_message());
                    }
                    return existing;
                })
                .flatMap(existing -> {
                    // Si le chef d'agence change, on attribue le rôle AGENCE_VOYAGE au nouveau chef
                    if (dto.getUser_id() == null) {
                        return agencePort.save(existing);
                    }
                    return userPersistencePort.findById(dto.getUser_id())
                            .switchIfEmpty(Mono.error(
                                    new ResourceNotFoundException("Nouveau chef d'agence non trouvé")))
                            .flatMap(newChef -> {
                                if (!newChef.getRoles().contains(RoleType.AGENCE_VOYAGE)) {
                                    List<RoleType> roles = new ArrayList<>(newChef.getRoles());
                                    roles.add(RoleType.AGENCE_VOYAGE);
                                    newChef.setRoles(roles);
                                    return userPersistencePort.save(newChef);
                                }
                                return Mono.just(newChef);
                            })
                            .then(agencePort.save(existing));
                })
                .map(agenceMapper::toDTO);
    }

    @Override
    @Transactional
    public Mono<Void> deleteAgenceVoyage(UUID agencyId, UUID currentUserId) {
        return agencePort.findById(agencyId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Agence non trouvée")))
                .filter(agence -> agence.getUserId().equals(currentUserId))
                .switchIfEmpty(Mono.error(
                        new UnauthorizeException(
                                "Vous n'êtes pas autorisé à supprimer cette agence")))
                .flatMap(agence -> agencePort.deleteById(agencyId));
    }

    @Override
    @Transactional
    public Mono<VehiculeDTO> addVehicule(UUID agenceId, VehiculeDTO vehiculeDTO, UUID currentUserId) {
        return agencePort.findById(agenceId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Agence non trouvée")))
                .filter(agence -> agence.getUserId().equals(currentUserId))
                .switchIfEmpty(Mono.error(
                        new UnauthorizeException(
                                "Vous n'êtes pas autorisé à ajouter un véhicule")))
                .flatMap(agence -> {
                    Vehicule vehicule = vehiculeMapper.toDomain(vehiculeDTO);
                    vehicule.setIdVehicule(null);
                    vehicule.setIdAgenceVoyage(agence.getAgencyId());
                    return agencePort.saveVehicule(vehicule);
                })
                .map(vehiculeMapper::toDTO);
    }

    @Override
    @Transactional
    public Mono<VehiculeDTO> updateVehicule(UUID vehiculeId, VehiculeDTO dto, UUID currentUserId) {
        return agencePort.findVehiculeById(vehiculeId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Véhicule non trouvé")))
                .flatMap(vehicule -> agencePort.findById(vehicule.getIdAgenceVoyage())
                        .filter(agence -> agence.getUserId().equals(currentUserId))
                        .switchIfEmpty(Mono.error(
                                new UnauthorizeException(
                                        "Vous n'êtes pas autorisé à modifier ce véhicule")))
                        .map(a -> vehicule))
                .map(existing -> {

                    if (dto.getNom() != null) {
                        existing.setNom(dto.getNom());
                    }

                    if (dto.getModele() != null) {
                        existing.setModele(dto.getModele());
                    }

                    if (dto.getDescription() != null) {
                        existing.setDescription(dto.getDescription());
                    }

                    if (dto.getNbrPlaces() > 0) {
                        existing.setNbrPlaces(dto.getNbrPlaces());
                    }

                    if (dto.getPlaqueMatricule() != null) {
                        existing.setPlaqueMatricule(dto.getPlaqueMatricule());
                    }

                    if (dto.getLienPhoto() != null) {
                        existing.setLienPhoto(dto.getLienPhoto());
                    }

                    return existing;
                })
                .flatMap(agencePort::saveVehicule)
                .map(vehiculeMapper::toDTO);
    }

    @Override
    public Mono<Void> deleteVehicule(UUID id, UUID currentUserId) {
        return agencePort.findVehiculeById(id)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Véhicule non trouvé")))
                .flatMap(vehicule -> agencePort.findById(vehicule.getIdAgenceVoyage())
                        .filter(agence -> agence.getUserId().equals(currentUserId))
                        .switchIfEmpty(Mono.error(
                                new UnauthorizeException(
                                        "Vous n'êtes pas autorisé à supprimer ce véhicule")))
                        .then(agencePort.deleteVehiculeById(id)));
    }

    @Override
    public Flux<VehiculeDTO> getVehiculesByAgence(UUID agenceId) {
        return agencePort.findVehiculesByAgenceId(agenceId)
                .map(vehiculeMapper::toDTO);
    }

    @Override
    public Mono<VehiculeDTO> getVehiculeById(UUID id) {
        return agencePort.findVehiculeById(id)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Véhicule non trouvé")))
                .map(vehiculeMapper::toDTO);
    }

    @Override
    @Transactional
    public Mono<PolitiqueAnnulation> createPolitique(UUID agenceId,
            PolitiqueAnnulation politique) {
        return agencePort.findById(agenceId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Agence non trouvée")))
                .flatMap(agence -> {

                    politique.setIdPolitique(UUID.randomUUID());
                    politique.setIdAgenceVoyage(agence.getAgencyId());

                    return agencePort.savePolitique(politique);
                });
    }

    @Override
    @Transactional
    public Mono<PolitiqueAnnulation> updatePolitique(UUID politiqueId,
            PolitiqueAnnulation input) {
        return agencePort.findPolitiqueById(politiqueId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Politique d'annulation introuvable")))
                .map(existing -> {
                    if (input.getListeTauxPeriode() != null) {
                        existing.setListeTauxPeriode(input.getListeTauxPeriode());
                    }

                    if (input.getDureeCoupon() != null) {
                        existing.setDureeCoupon(input.getDureeCoupon());
                    }

                    return existing;
                })
                .flatMap(agencePort::savePolitique);
    }

    @Override
    public Mono<PolitiqueAnnulation> getPolitiqueById(UUID id) {
        return agencePort.findPolitiqueById(id)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Politique d'annulation introuvable")));
    }

    @Override
    public Flux<PolitiqueAnnulation> getAllPolitiquesByAgence(UUID agenceId) {
        return agencePort.findPolitiqueByAgenceId(agenceId)
                .flux();
    }

    @Override
    public Flux<AgenceVoyagePreviewDTO> getAgencesByGareRoutiereId(UUID gareRoutiereId) {
        return agencePort.findByGareRoutiereId(gareRoutiereId).map(agenceMapper::toPreviewDTO);
    }

    @Override
    public Mono<AgenceVoyageResponseDTO> updateStatutAgence(UUID agenceId, boolean active, String motif, UUID currentUserId) {
        return agencePort.findById(agenceId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Agence non trouvée")))
                .flatMap(agence -> {
                    agence.setIsActive(active);
                    return agencePort.save(agence);
                })
                .map(agenceMapper::toResponseDTO);
    }

    @Override
    public Mono<AgenceVoyageResponseDTO> updateMoyensPaiement(UUID agenceId, List<String> moyensPaiement, UUID currentUserId) {
        return agencePort.findById(agenceId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Agence non trouvée")))
                .flatMap(agence -> {
                    agence.setMoyensPaiement(moyensPaiement);
                    return agencePort.save(agence);
                })
                .map(agenceMapper::toResponseDTO);
    }

    @Override
    public Mono<AgenceVoyageResponseDTO> updateRessourcesDefaut(UUID agenceId, UUID vehiculeIdDefaut, UUID chauffeurIdDefaut, UUID currentUserId) {
        return agencePort.findById(agenceId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Agence non trouvée")))
                .flatMap(agence -> {
                    agence.setVehiculeIdDefaut(vehiculeIdDefaut);
                    agence.setChauffeurIdDefaut(chauffeurIdDefaut);
                    return agencePort.save(agence);
                })
                .map(agenceMapper::toResponseDTO);
    }
}
