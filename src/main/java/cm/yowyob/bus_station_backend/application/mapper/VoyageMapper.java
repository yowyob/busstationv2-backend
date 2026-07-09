package cm.yowyob.bus_station_backend.application.mapper;

import cm.yowyob.bus_station_backend.application.dto.classVoyage.ClassVoyageDTO;
import cm.yowyob.bus_station_backend.application.dto.classVoyage.ClassVoyageResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.vehicule.VehiculeDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageCreateRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageDetailsDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyagePreviewDTO;
import cm.yowyob.bus_station_backend.application.dto.user.UserResponseDTO;
import cm.yowyob.bus_station_backend.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VoyageMapper {

    public Voyage toDomain(VoyageCreateRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Voyage voyage = Voyage.builder()
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .dateDepartPrev(dto.getDateDepartPrev())
                .lieuDepart(dto.getLieuDepart())
                .lieuArrive(dto.getLieuArrive())
                .heureArrive(dto.getHeureArrive())
                .pointDeDepart(dto.getPointDeDepart())
                .pointArrivee(dto.getPointArrivee())
                .nbrPlaceReservable(dto.getNbrPlaceReservable())
                .heureDepartEffectif(dto.getHeureDepartEffectif())
                .nbrPlaceReserve(dto.getNbrPlaceReserve())
                .nbrPlaceConfirm(dto.getNbrPlaceConfirm())
                .nbrPlaceRestante(dto.getNbrPlaceRestante())
                .dateLimiteReservation(dto.getDateLimiteReservation())
                .dateLimiteConfirmation(dto.getDateLimiteConfirmation())
                .statusVoyage(dto.getStatusVoyage())
                .smallImage(dto.getSmallImage())
                .bigImage(dto.getBigImage())
                .build();

        // Utilisation de la méthode métier pour transformer la liste en String JSON/CSV interne
        voyage.setAmenities(dto.getAmenities());

        return voyage;
    }

    public VoyageDTO toDTO(Voyage domain) {
        if (domain == null) {
            return null;
        }

        VoyageDTO dto = new VoyageDTO();
        dto.setTitre(domain.getTitre());
        dto.setDescription(domain.getDescription());
        dto.setDateDepartPrev(domain.getDateDepartPrev());
        dto.setLieuDepart(domain.getLieuDepart());
        dto.setLieuArrive(domain.getLieuArrive());
        dto.setHeureDepartEffectif(domain.getHeureDepartEffectif());
        dto.setDureeVoyage(domain.getDureeVoyage());
        dto.setHeureArrive(domain.getHeureArrive());
        dto.setDatePublication(domain.getDatePublication());
        dto.setDateLimiteReservation(domain.getDateLimiteReservation());
        dto.setDateLimiteConfirmation(domain.getDateLimiteConfirmation());
        dto.setStatusVoyage(domain.getStatusVoyage());
        dto.setSmallImage(domain.getSmallImage());
        dto.setBigImage(domain.getBigImage());
        dto.setAmenities(domain.getAmenities());

        return dto;
    }

    public VoyagePreviewDTO toPreviewDTO(Voyage domain) {
        if (domain == null) {
            return null;
        }

        VoyagePreviewDTO dto = new VoyagePreviewDTO();
        dto.setIdVoyage(domain.getIdVoyage());
        dto.setLieuDepart(domain.getLieuDepart());
        dto.setLieuArrive(domain.getLieuArrive());
        dto.setNbrPlaceRestante(domain.getNbrPlaceRestante());
        dto.setNbrPlaceReservable(domain.getNbrPlaceReservable());
        dto.setDateDepartPrev(domain.getDateDepartPrev());
        dto.setDureeVoyage(domain.getDureeVoyage());
        dto.setSmallImage(domain.getSmallImage());
        dto.setBigImage(domain.getBigImage());
        dto.setAmenities(domain.getAmenities());
        dto.setStatusVoyage(domain.getStatusVoyage());

        return dto;
    }

    /**
     * Enrichit un VoyagePreviewDTO avec les informations d'agence et de classe voyage
     */
    public VoyagePreviewDTO enrichPreviewDTO(VoyagePreviewDTO dto, AgenceVoyage agence, ClassVoyage classVoyage) {
        if (dto == null) {
            return null;
        }

        if (agence != null) {
            dto.setNomAgence(agence.getLongName());
        }

        if (classVoyage != null) {
            dto.setNomClasseVoyage(classVoyage.getNom());
            dto.setPrix(classVoyage.getPrix());
        }

        return dto;
    }

    public VoyageDetailsDTO toDetailsDTO(Voyage domain) {
        if (domain == null) {
            return null;
        }

        VoyageDetailsDTO dto = new VoyageDetailsDTO();
        dto.setIdVoyage(domain.getIdVoyage());
        dto.setTitre(domain.getTitre());
        dto.setDescription(domain.getDescription());
        dto.setDateDepartPrev(domain.getDateDepartPrev());
        dto.setLieuDepart(domain.getLieuDepart());
        dto.setDateDepartEffectif(domain.getDateDepartEffectif());
        dto.setDateArriveEffectif(domain.getDateArriveEffectif());
        dto.setLieuArrive(domain.getLieuArrive());
        dto.setHeureDepartEffectif(domain.getHeureDepartEffectif());
        dto.setDureeVoyage(domain.getDureeVoyage());
        dto.setHeureArrive(domain.getHeureArrive());
        dto.setNbrPlaceReservable(domain.getNbrPlaceReservable());
        dto.setNbrPlaceRestante(domain.getNbrPlaceRestante());
        dto.setDatePublication(domain.getDatePublication());
        dto.setDateLimiteReservation(domain.getDateLimiteReservation());
        dto.setDateLimiteConfirmation(domain.getDateLimiteConfirmation());
        dto.setStatusVoyage(domain.getStatusVoyage());
        dto.setSmallImage(domain.getSmallImage());
        dto.setBigImage(domain.getBigImage());
        dto.setPointDeDepart(domain.getPointDeDepart());
        dto.setPointArrivee(domain.getPointArrivee());
        dto.setAmenities(domain.getAmenities());

        return dto;
    }

    /**
     * Enrichit un VoyageDetailsDTO avec toutes les informations complémentaires
     * (Agence, ClassVoyage, Vehicule, Chauffeur, Places réservées)
     */
    public VoyageDetailsDTO enrichDetailsDTO(
            VoyageDetailsDTO dto,
            AgenceVoyage agence,
            ClassVoyage classVoyage,
            Vehicule vehicule,
            ChauffeurAgenceVoyage chauffeur,
            List<Integer> placesReservees) {

        if (dto == null) {
            return null;
        }

        if (agence != null) {
            dto.setNomAgence(agence.getLongName());
        }

        if (classVoyage != null) {
            dto.setNomClasseVoyage(classVoyage.getNom());
            dto.setPrix(classVoyage.getPrix());
        }

        if (vehicule != null) {
            dto.setVehicule(vehicule);
        }

        if (chauffeur != null && chauffeur.getUserId() != null) {
            // Le UserResponseDTO doit être construit par le service
            // via le UserMapper à partir du User récupéré
            // dto.setChauffeur(userMapper.toResponseDTO(user));
        }

        if (placesReservees != null) {
            dto.setPlaceReservees(placesReservees);
        }

        return dto;
    }

    /**
     * Version simplifiée pour enrichir avec Vehicule déjà mappé
     */
    public VoyageDetailsDTO enrichDetailsDTOWithMappedEntities(
            VoyageDetailsDTO dto,
            String nomAgence,
            String nomClasseVoyage,
            double prix,
            Vehicule vehicule,
            UserResponseDTO chauffeurDTO,
            List<Integer> placesReservees) {

        if (dto == null) {
            return null;
        }

        dto.setNomAgence(nomAgence);
        dto.setNomClasseVoyage(nomClasseVoyage);
        dto.setPrix(prix);
        dto.setVehicule(vehicule);
        dto.setChauffeur(chauffeurDTO);
        dto.setPlaceReservees(placesReservees);

        return dto;
    }

    /**
     * Convertit un Vehicule domain en vehicule DTO
     */
    public VehiculeDTO mapVehiculeForDTO(Vehicule vehicule) {
        if (vehicule == null) {
            return null;
        }

        VehiculeDTO vehiculeDTO = new VehiculeDTO();
        vehiculeDTO.setIdAgenceVoyage(vehicule.getIdAgenceVoyage());
        vehiculeDTO.setNom(vehicule.getNom());
        vehiculeDTO.setModele(vehicule.getModele());
        vehiculeDTO.setNbrPlaces(vehicule.getNbrPlaces());
        vehiculeDTO.setLienPhoto(vehicule.getLienPhoto());
        vehiculeDTO.setPlaqueMatricule(vehicule.getPlaqueMatricule());
        return vehiculeDTO;
    }

    public ClassVoyageDTO mapToClassVoyageDTO(ClassVoyage classVoyage){
        if (classVoyage == null){
            return null;
        }

        ClassVoyageDTO classVoyageDTO = new ClassVoyageDTO();
        classVoyageDTO.setPrix(classVoyage.getPrix());
        classVoyageDTO.setNom(classVoyage.getNom());
        return classVoyageDTO;
    }

    public ClassVoyageResponseDTO mapToClassVoyageResponseDTO(ClassVoyage classVoyage){
        if (classVoyage == null){
            return null;
        }

        ClassVoyageResponseDTO responseDTO = new ClassVoyageResponseDTO();
        responseDTO.setId(classVoyage.getIdClassVoyage());
        responseDTO.setPrix(classVoyage.getPrix());
        responseDTO.setNom(classVoyage.getNom());
        responseDTO.setIdAgenceVoyage(classVoyage.getIdAgenceVoyage());
        return responseDTO;
    }
}