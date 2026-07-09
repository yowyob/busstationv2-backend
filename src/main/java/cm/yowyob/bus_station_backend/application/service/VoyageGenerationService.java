package cm.yowyob.bus_station_backend.application.service;

import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageBrouillonCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageBrouillonResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageCreateRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageDetailsDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.generation.*;
import cm.yowyob.bus_station_backend.application.port.in.VoyageBrouillonUseCase;
import cm.yowyob.bus_station_backend.application.port.in.VoyageGenerationUseCase;
import cm.yowyob.bus_station_backend.application.port.in.VoyageUseCase;
import cm.yowyob.bus_station_backend.application.port.out.AgencePersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.PlanningPersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.VoyagePersistencePort;
import cm.yowyob.bus_station_backend.domain.model.AgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.CreneauPlanning;
import cm.yowyob.bus_station_backend.domain.model.Vehicule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoyageGenerationService implements VoyageGenerationUseCase {

    private final PlanningPersistencePort planningPort;
    private final AgencePersistencePort agencePort;
    private final VoyagePersistencePort voyagePort;
    private final VoyageUseCase voyageUseCase;
    private final VoyageBrouillonUseCase brouillonUseCase;

    // ============== 1. GENERATION UNITAIRE ==============

    @Override
    public Mono<GenerationResultDTO> genererUnitaire(GenerationUnitaireRequestDTO request, UUID currentUserId) {
        return planningPort.findCreneauById(request.getLigneServiceId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Ligne de service introuvable : " + request.getLigneServiceId())))
                .flatMap(creneau -> resolveAgenceFromCreneau(creneau)
                        .flatMap(agence -> tenterGeneration(creneau, agence, request.getDateDepartPrev(),
                                request.isPublierDirectement(), currentUserId)));
    }

    // ============== 2. MATCHING PREVIEW ==============

    @Override
    public Mono<MatchingPreviewResponseDTO> matchingPreview(GenerationSemaineRequestDTO request) {
        return agencePort.findById(request.getAgenceId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Agence introuvable : " + request.getAgenceId())))
                .flatMap(agence -> Flux.fromIterable(request.getLignesIds())
                        .flatMap(ligneId -> previewOneLigne(ligneId, agence, request.getSemaineDebut()))
                        .collectList()
                        .map(items -> {
                            int publie = 0, incomplet = 0, ignore = 0;
                            List<MatchingPreviewItemDTO> kept = new ArrayList<>();
                            for (MatchingPreviewItemDTO it : items) {
                                if (it == null) { ignore++; continue; }
                                kept.add(it);
                                if ("PUBLIE".equals(it.getStatutPrevu())) publie++;
                                else incomplet++;
                            }
                            return MatchingPreviewResponseDTO.builder()
                                    .voyagesPreview(kept)
                                    .totalPublie(publie)
                                    .totalIncomplet(incomplet)
                                    .totalIgnore(ignore)
                                    .build();
                        }));
    }

    // ============== 3. GENERER SEMAINE ==============

    @Override
    public Mono<GenerationSemaineResponseDTO> genererSemaine(GenerationSemaineRequestDTO request, UUID currentUserId) {
        return agencePort.findById(request.getAgenceId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Agence introuvable : " + request.getAgenceId())))
                .flatMap(agence -> Flux.fromIterable(request.getLignesIds())
                        .flatMap(ligneId -> genererPourLigneSurSemaine(ligneId, agence,
                                request.getSemaineDebut(), currentUserId))
                        .collectList()
                        .map(results -> {
                            List<UUID> publies = new ArrayList<>();
                            List<UUID> brouillons = new ArrayList<>();
                            List<String> erreurs = new ArrayList<>();
                            for (GenerationResultDTO r : results) {
                                if (r == null) continue;
                                if (r.getVoyageId() != null) publies.add(r.getVoyageId());
                                if (r.getBrouillonId() != null) brouillons.add(r.getBrouillonId());
                                if (r.getMessage() != null && r.getMessage().startsWith("ERREUR"))
                                    erreurs.add(r.getMessage());
                            }
                            return GenerationSemaineResponseDTO.builder()
                                    .voyagesPubliesIds(publies)
                                    .brouillonsCreesIds(brouillons)
                                    .erreurs(erreurs)
                                    .totalPublie(publies.size())
                                    .totalBrouillons(brouillons.size())
                                    .build();
                        }));
    }

    // ============== HELPERS ==============

    /**
     * Pour un creneau dont on a déjà l'agence, tente :
     * 1. Calcule la fenêtre [dateDepart, heureArrivee]
     * 2. Résout véhicule/chauffeur (créneau OU défauts agence)
     * 3. Vérifie disponibilité
     * 4. Si tout OK + publierDirectement → crée voyage PUBLIE
     *    Sinon → crée VoyageBrouillon (INCOMPLET ou PRET selon ce qui est rempli)
     */
    private Mono<GenerationResultDTO> tenterGeneration(CreneauPlanning creneau, AgenceVoyage agence,
                                                       LocalDate date, boolean publierDirectement,
                                                       UUID currentUserId) {
        if (creneau.getHeureDepart() == null) {
            return Mono.just(buildIncompletResult(creneau, null, agence, null, null,
                    List.of("Le créneau n'a pas d'heure de départ définie"), null));
        }

        LocalDateTime dateDepart = LocalDateTime.of(date, creneau.getHeureDepart());
        LocalDateTime heureArrivee = creneau.getHeureArrivee() != null
                ? LocalDateTime.of(date, creneau.getHeureArrivee())
                : dateDepart.plusHours(4);
        if (heureArrivee.isBefore(dateDepart)) {
            heureArrivee = heureArrivee.plusDays(1);
        }
        final LocalDateTime fenetreDebut = dateDepart;
        final LocalDateTime fenetreFin = heureArrivee;

        UUID vehiculeId = creneau.getIdVehicule() != null
                ? creneau.getIdVehicule() : agence.getVehiculeIdDefaut();
        UUID chauffeurId = creneau.getIdChauffeur() != null
                ? creneau.getIdChauffeur() : agence.getChauffeurIdDefaut();
        UUID classVoyageId = creneau.getIdClassVoyage();

        List<String> conflits = new ArrayList<>();
        if (vehiculeId == null) conflits.add("Aucun véhicule (ni dans la ligne ni en défaut agence)");
        if (chauffeurId == null) conflits.add("Aucun chauffeur (ni dans la ligne ni en défaut agence)");
        if (classVoyageId == null) conflits.add("Aucune classe de voyage définie sur la ligne");

        // Vérification de disponibilité (uniquement si on a les ressources)
        Mono<Boolean> vehDispo = vehiculeId != null
                ? voyagePort.findVoyagesUsingVehiculeBetween(vehiculeId, fenetreDebut, fenetreFin)
                        .hasElements().map(b -> !b)
                : Mono.just(false);
        Mono<Boolean> chaDispo = chauffeurId != null
                ? voyagePort.findVoyagesUsingChauffeurBetween(chauffeurId, fenetreDebut, fenetreFin)
                        .hasElements().map(b -> !b)
                : Mono.just(false);

        final UUID vehFinal = vehiculeId;
        final UUID chaFinal = chauffeurId;

        return Mono.zip(vehDispo, chaDispo)
                .flatMap(t -> {
                    boolean vehOK = t.getT1();
                    boolean chaOK = t.getT2();
                    if (vehFinal != null && !vehOK) conflits.add("Véhicule déjà affecté sur cette plage horaire");
                    if (chaFinal != null && !chaOK) conflits.add("Chauffeur déjà affecté sur cette plage horaire");

                    boolean matchingComplet = conflits.isEmpty();

                    if (matchingComplet && publierDirectement) {
                        return creerVoyagePublie(creneau, agence, dateDepart, fenetreFin,
                                vehFinal, chaFinal, classVoyageId, currentUserId);
                    } else {
                        // On garde les ressources trouvées même si incomplet (ex: vehicule OK mais chauffeur KO)
                        return creerBrouillon(creneau, agence, dateDepart, fenetreFin,
                                vehFinal, chaFinal,
                                classVoyageId, conflits);
                    }
                });
    }

    private Mono<GenerationResultDTO> creerVoyagePublie(CreneauPlanning creneau, AgenceVoyage agence,
                                                        LocalDateTime dateDepart, LocalDateTime heureArrive,
                                                        UUID vehiculeId, UUID chauffeurId, UUID classVoyageId,
                                                        UUID currentUserId) {
        // On a besoin de nbrPlaces — on lit le véhicule.
        return agencePort.findVehiculeById(vehiculeId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Véhicule introuvable : " + vehiculeId)))
                .flatMap(vehicule -> {
                    VoyageCreateRequestDTO dto = new VoyageCreateRequestDTO();
                    dto.setTitre(creneau.getTitre() != null
                            ? creneau.getTitre()
                            : creneau.getLieuDepart() + " → " + creneau.getLieuArrive());
                    dto.setDescription(creneau.getDescription() != null
                            ? creneau.getDescription() : dto.getTitre());
                    dto.setDateDepartPrev(dateDepart);
                    dto.setHeureDepartEffectif(dateDepart);
                    dto.setHeureArrive(heureArrive);
                    dto.setLieuDepart(creneau.getLieuDepart());
                    dto.setLieuArrive(creneau.getLieuArrive());
                    dto.setPointDeDepart(creneau.getPointDeDepart() != null
                            ? creneau.getPointDeDepart() : creneau.getLieuDepart());
                    dto.setPointArrivee(creneau.getPointArrivee() != null
                            ? creneau.getPointArrivee() : creneau.getLieuArrive());

                    int places = creneau.getNbrPlacesDisponibles() > 0
                            ? creneau.getNbrPlacesDisponibles()
                            : (vehicule.getNbrPlaces() > 0 ? vehicule.getNbrPlaces() : 50);
                    dto.setNbrPlaceReservable(places);
                    dto.setNbrPlaceReserve(0);
                    dto.setNbrPlaceConfirm(0);
                    dto.setNbrPlaceRestante(places);

                    int delaiResa = creneau.getDelaiReservationHeures() > 0
                            ? creneau.getDelaiReservationHeures() : 2;
                    int delaiConf = creneau.getDelaiConfirmationHeures() > 0
                            ? creneau.getDelaiConfirmationHeures() : 1;
                    dto.setDateLimiteReservation(dateDepart.minusHours(delaiResa));
                    dto.setDateLimiteConfirmation(dateDepart.minusHours(delaiConf));

                    dto.setSmallImage(creneau.getSmallImage());
                    dto.setBigImage(creneau.getBigImage());
                    dto.setChauffeurId(chauffeurId);
                    dto.setVehiculeId(vehiculeId);
                    dto.setClassVoyageId(classVoyageId);
                    dto.setAgenceVoyageId(agence.getAgencyId());

                    return voyageUseCase.createVoyage(dto, currentUserId)
                            .flatMap(created -> voyageUseCase.updateVoyageStatus(
                                    created.getIdVoyage(), "PUBLIE", currentUserId))
                            .map(published -> GenerationResultDTO.builder()
                                    .ligneServiceId(creneau.getIdCreneau())
                                    .dateDepartPrev(dateDepart)
                                    .statut("PUBLIE")
                                    .voyageId(published.getIdVoyage())
                                    .vehiculeId(vehiculeId)
                                    .chauffeurId(chauffeurId)
                                    .classVoyageId(classVoyageId)
                                    .conflits(List.of())
                                    .message("Voyage publié avec succès")
                                    .build())
                            .onErrorResume(e -> {
                                log.warn("Echec création voyage pour ligne {} : {}",
                                        creneau.getIdCreneau(), e.getMessage());
                                return Mono.just(GenerationResultDTO.builder()
                                        .ligneServiceId(creneau.getIdCreneau())
                                        .dateDepartPrev(dateDepart)
                                        .statut("INCOMPLET")
                                        .conflits(List.of("Erreur lors de la publication : " + e.getMessage()))
                                        .message("ERREUR : " + e.getMessage())
                                        .build());
                            });
                });
    }

    private Mono<GenerationResultDTO> creerBrouillon(CreneauPlanning creneau, AgenceVoyage agence,
                                                     LocalDateTime dateDepart, LocalDateTime heureArrive,
                                                     UUID vehiculeId, UUID chauffeurId, UUID classVoyageId,
                                                     List<String> conflits) {
        VoyageBrouillonCreateDTO dto = new VoyageBrouillonCreateDTO();
        dto.setAgenceVoyageId(agence.getAgencyId());
        dto.setLigneServiceId(creneau.getIdCreneau());
        dto.setTitre(creneau.getTitre() != null
                ? creneau.getTitre()
                : creneau.getLieuDepart() + " → " + creneau.getLieuArrive());
        dto.setDescription(creneau.getDescription());
        dto.setLieuDepart(creneau.getLieuDepart());
        dto.setLieuArrive(creneau.getLieuArrive());
        dto.setPointDeDepart(creneau.getPointDeDepart());
        dto.setPointArrivee(creneau.getPointArrivee());
        dto.setDateDepartPrev(dateDepart);
        dto.setHeureDepartEffectif(dateDepart);
        dto.setHeureArrive(heureArrive);
        dto.setClassVoyageId(classVoyageId);
        dto.setVehiculeId(vehiculeId);
        dto.setChauffeurId(chauffeurId);
        if (creneau.getNbrPlacesDisponibles() > 0)
            dto.setNbrPlaceReservable(creneau.getNbrPlacesDisponibles());
        dto.setSmallImage(creneau.getSmallImage());
        dto.setBigImage(creneau.getBigImage());
        dto.setNotes("Généré automatiquement depuis ligne " + creneau.getIdCreneau()
                + (conflits.isEmpty() ? "" : " — conflits : " + String.join(" | ", conflits)));

        return brouillonUseCase.create(dto)
                .map(brouillon -> GenerationResultDTO.builder()
                        .ligneServiceId(creneau.getIdCreneau())
                        .dateDepartPrev(dateDepart)
                        .statut("INCOMPLET")
                        .brouillonId(brouillon.getId())
                        .vehiculeId(vehiculeId)
                        .chauffeurId(chauffeurId)
                        .classVoyageId(classVoyageId)
                        .conflits(conflits)
                        .message("Brouillon créé : " + brouillon.getStatutBrouillon())
                        .build());
    }

    /**
     * Pour le preview : ne persiste rien, retourne juste l'item avec statut prévu.
     * Retourne null si la ligne ne tombe pas dans la semaine (jourSemaine inconnu).
     */
    private Mono<MatchingPreviewItemDTO> previewOneLigne(UUID ligneId, AgenceVoyage agence, LocalDate semaineDebut) {
        return planningPort.findCreneauById(ligneId)
                .flatMap(creneau -> {
                    LocalDate date = trouverDateDansSemaine(creneau, semaineDebut);
                    if (date == null || creneau.getHeureDepart() == null) {
                        return Mono.empty(); // ignoré
                    }
                    LocalDateTime dateDepart = LocalDateTime.of(date, creneau.getHeureDepart());
                    LocalDateTime heureArrivee = creneau.getHeureArrivee() != null
                            ? LocalDateTime.of(date, creneau.getHeureArrivee())
                            : dateDepart.plusHours(4);
                    if (heureArrivee.isBefore(dateDepart)) heureArrivee = heureArrivee.plusDays(1);

                    UUID vehiculeId = creneau.getIdVehicule() != null
                            ? creneau.getIdVehicule() : agence.getVehiculeIdDefaut();
                    UUID chauffeurId = creneau.getIdChauffeur() != null
                            ? creneau.getIdChauffeur() : agence.getChauffeurIdDefaut();
                    UUID classVoyageId = creneau.getIdClassVoyage();

                    List<String> conflits = new ArrayList<>();
                    if (vehiculeId == null) conflits.add("Aucun véhicule");
                    if (chauffeurId == null) conflits.add("Aucun chauffeur");
                    if (classVoyageId == null) conflits.add("Aucune classe");

                    final LocalDateTime fenetreDebut = dateDepart;
                    final LocalDateTime fenetreFin = heureArrivee;
                    final UUID vehFinal = vehiculeId;
                    final UUID chaFinal = chauffeurId;

                    Mono<Boolean> vehDispo = vehFinal != null
                            ? voyagePort.findVoyagesUsingVehiculeBetween(vehFinal, fenetreDebut, fenetreFin)
                                    .hasElements().map(b -> !b)
                            : Mono.just(false);
                    Mono<Boolean> chaDispo = chaFinal != null
                            ? voyagePort.findVoyagesUsingChauffeurBetween(chaFinal, fenetreDebut, fenetreFin)
                                    .hasElements().map(b -> !b)
                            : Mono.just(false);

                    return Mono.zip(vehDispo, chaDispo).map(t -> {
                        if (vehFinal != null && !t.getT1()) conflits.add("Véhicule en conflit");
                        if (chaFinal != null && !t.getT2()) conflits.add("Chauffeur en conflit");
                        String statut = conflits.isEmpty() ? "PUBLIE" : "INCOMPLET";
                        return MatchingPreviewItemDTO.builder()
                                .ligneServiceId(creneau.getIdCreneau())
                                .titre(creneau.getTitre() != null ? creneau.getTitre()
                                        : creneau.getLieuDepart() + " → " + creneau.getLieuArrive())
                                .lieuDepart(creneau.getLieuDepart())
                                .lieuArrive(creneau.getLieuArrive())
                                .dateDepartPrev(dateDepart)
                                .heureArrive(fenetreFin)
                                .vehiculeMatcheId(vehFinal)
                                .chauffeurMatcheId(chaFinal)
                                .classVoyageId(classVoyageId)
                                .statutPrevu(statut)
                                .conflits(conflits)
                                .build();
                    });
                });
    }

    private Mono<GenerationResultDTO> genererPourLigneSurSemaine(UUID ligneId, AgenceVoyage agence,
                                                                  LocalDate semaineDebut, UUID currentUserId) {
        return planningPort.findCreneauById(ligneId)
                .flatMap(creneau -> {
                    LocalDate date = trouverDateDansSemaine(creneau, semaineDebut);
                    if (date == null) {
                        return Mono.just(GenerationResultDTO.builder()
                                .ligneServiceId(ligneId)
                                .statut("IGNORE")
                                .conflits(List.of("Le créneau n'a pas de jourSemaine — ignoré pour la semaine"))
                                .message("ERREUR : ligne sans jourSemaine, ignorée")
                                .build());
                    }
                    return tenterGeneration(creneau, agence, date, true, currentUserId);
                })
                .switchIfEmpty(Mono.just(GenerationResultDTO.builder()
                        .ligneServiceId(ligneId)
                        .statut("IGNORE")
                        .message("ERREUR : ligne " + ligneId + " introuvable")
                        .build()));
    }

    /**
     * Trouve la date dans [semaineDebut, semaineDebut+6] qui correspond
     * au jourSemaine du créneau. Retourne null si jourSemaine n'est pas dans cette semaine
     * (ne devrait pas arriver vu que la semaine couvre les 7 jours, mais sécurité).
     */
    private LocalDate trouverDateDansSemaine(CreneauPlanning creneau, LocalDate semaineDebut) {
        DayOfWeek jour = creneau.getJourSemaine();
        if (jour == null) return null;
        for (int i = 0; i < 7; i++) {
            LocalDate d = semaineDebut.plusDays(i);
            if (d.getDayOfWeek() == jour) return d;
        }
        return null;
    }

    /**
     * Charge l'agence depuis le créneau (en passant par son planning).
     */
    private Mono<AgenceVoyage> resolveAgenceFromCreneau(CreneauPlanning creneau) {
        if (creneau.getIdPlanning() == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Créneau orphelin (pas de planning) : " + creneau.getIdCreneau()));
        }
        return planningPort.findById(creneau.getIdPlanning())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Planning introuvable : " + creneau.getIdPlanning())))
                .flatMap(planning -> agencePort.findById(planning.getIdAgenceVoyage())
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Agence introuvable : " + planning.getIdAgenceVoyage()))));
    }

    private GenerationResultDTO buildIncompletResult(CreneauPlanning creneau, LocalDateTime dateDepart,
                                                     AgenceVoyage agence, UUID vehiculeId, UUID chauffeurId,
                                                     List<String> conflits, UUID brouillonId) {
        return GenerationResultDTO.builder()
                .ligneServiceId(creneau.getIdCreneau())
                .dateDepartPrev(dateDepart)
                .statut("INCOMPLET")
                .brouillonId(brouillonId)
                .vehiculeId(vehiculeId)
                .chauffeurId(chauffeurId)
                .classVoyageId(creneau.getIdClassVoyage())
                .conflits(conflits)
                .message("Matching incomplet : " + String.join(" | ", conflits))
                .build();
    }
}