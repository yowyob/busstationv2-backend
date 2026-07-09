package cm.yowyob.bus_station_backend.application.port.in;

import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageDTO;
import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyagePreviewDTO;
import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.vehicule.VehiculeDTO;
import cm.yowyob.bus_station_backend.domain.model.PolitiqueAnnulation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AgenceUseCase {
    // Agence Info
    Mono<AgenceVoyageResponseDTO> getAgenceById(UUID id); // Changé pour retourner un DTO de réponse détaillé

    Mono<Page<AgenceVoyageResponseDTO>> getAllAgences(Pageable pageable);

    Mono<AgenceVoyageResponseDTO> getAgenceByChefAgenceId(UUID chefId); // Changé pour retourner un DTO
    // Correspond à createAgenceVoyage

    Mono<AgenceVoyageDTO> updateAgence(UUID id, AgenceVoyageDTO agenceDTO, UUID currentUserId); // Changé pour prendre
                                                                                                // un DTO et retourner
                                                                                                // un DTO

    Mono<AgenceVoyageResponseDTO> createAgence(AgenceVoyageDTO agenceDTO);

    Mono<Void> deleteAgenceVoyage(UUID agencyId, UUID currentUserId);

    // Gestion Véhicules
    Mono<VehiculeDTO> addVehicule(UUID agenceId, VehiculeDTO vehiculeDTO, UUID currentUserId); // Ajout de agenceId dans
                                                                                               // la signature, retour
                                                                                               // DTO

    Mono<VehiculeDTO> updateVehicule(UUID vehiculeId, VehiculeDTO vehiculeDTO, UUID currentUserId); // Prise d'ID et
                                                                                                    // DTO, retour DTO

    Mono<Void> deleteVehicule(UUID id, UUID currentUserId);

    Flux<VehiculeDTO> getVehiculesByAgence(UUID agenceId); // Retour Flux<DTO>

    Mono<VehiculeDTO> getVehiculeById(UUID id); // Retour DTO

    // Politique Annulation
    Mono<PolitiqueAnnulation> createPolitique(UUID agenceId, PolitiqueAnnulation politique); // Ajout de agenceId

    Mono<PolitiqueAnnulation> updatePolitique(UUID politiqueId, PolitiqueAnnulation politique); // Ajout de politiqueId
                                                                                                // pour update
                                                                                                // spécifique

    Mono<PolitiqueAnnulation> getPolitiqueById(UUID id);

    Flux<PolitiqueAnnulation> getAllPolitiquesByAgence(UUID agenceId); // Filtré par agence
    // Nouveau: Mono<PolitiqueAnnulation> getPolitiqueByAgenceId(UUID agenceId); //
    // Pour récupérer la politique unique d'une agence

    Flux<AgenceVoyagePreviewDTO> getAgencesByGareRoutiereId(UUID gareRoutiereId); // Récupérer les agences
    // d'une gare routiere

    Mono<AgenceVoyageResponseDTO> updateStatutAgence(UUID agenceId, boolean active, String motif, UUID currentUserId);

    Mono<AgenceVoyageResponseDTO> updateMoyensPaiement(UUID agenceId, java.util.List<String> moyensPaiement, UUID currentUserId);

    Mono<AgenceVoyageResponseDTO> updateRessourcesDefaut(UUID agenceId, UUID vehiculeIdDefaut, UUID chauffeurIdDefaut, UUID currentUserId);
}