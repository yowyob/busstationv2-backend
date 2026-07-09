package cm.yowyob.bus_station_backend.application.mapper;

import cm.yowyob.bus_station_backend.application.dto.reservation.*;
import cm.yowyob.bus_station_backend.domain.enums.Gender;
import cm.yowyob.bus_station_backend.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ReservationMapper {
    public Reservation toDomain(ReservationDTO dto) {
        if (dto == null) return null;

        Reservation reservation = new Reservation();
        // ID généré ici ou dans le service, mais mieux vaut le faire tôt
        reservation.setIdReservation(UUID.randomUUID());
        reservation.setIdUser(dto.getIdUser());
        reservation.setIdVoyage(dto.getIdVoyage());
        reservation.setNbrPassager(dto.getNbrPassager());
        reservation.setMontantPaye(dto.getMontantPaye());
        // Les status par défaut sont gérés dans le service ou le constructeur du domaine
        return reservation;
    }

    public List<Passager> toPassagerDomainList(PassagerDTO[] passagerDTOs, UUID reservationId) {
        if (passagerDTOs == null) return List.of();

        return Arrays.stream(passagerDTOs)
                .map(dto -> {
                    Passager p = new Passager();
                    p.setIdPassager(UUID.randomUUID());
                    p.setNumeroPieceIdentific(dto.getNumeroPieceIdentific());
                    p.setNom(dto.getNom());
                    p.setGenre(Gender.valueOf(dto.getGenre()));
                    p.setAge(dto.getAge());
                    p.setNbrBaggage(dto.getNbrBaggage());
                    p.setPlaceChoisis(dto.getPlaceChoisis());
                    p.setIdReservation(reservationId);
                    return p;
                })
                .collect(Collectors.toList());
    }

    public ReservationDetailDTO toDetailDTO(Reservation reservation, List<Passager> passagers,
                                            Voyage voyage, AgenceVoyage agence, Vehicule vehicule) {
        ReservationDetailDTO dto = new ReservationDetailDTO(reservation);
        dto.setPassager(passagers);
        dto.setVoyage(voyage);
        dto.setAgence(agence); // Type générique ou DTO spécifique selon votre projet
        dto.setVehicule(vehicule);
        return dto;
    }

    public ReservationPreviewDTO toPreviewDTO(Reservation reservation, Voyage voyage, AgenceVoyage agence) {
        // Supposons que le constructeur de ReservationPreviewDTO accepte ces paramètres
        return new ReservationPreviewDTO(reservation, voyage, agence);
    }

    public BilletDTO toBilletDTO(Reservation reservation,
                                 Passager passager,
                                 Voyage voyage,
                                 ClassVoyage classe,
                                 AgenceVoyage agence) {
        if (reservation == null || passager == null || voyage == null || classe == null || agence == null) {
            return null;
        }

        BilletDTO billetDTO = new BilletDTO();

        // Informations du voyage
        billetDTO.setTitre(voyage.getTitre());
        billetDTO.setDescription(voyage.getDescription());
        billetDTO.setDateDepartPrev(voyage.getDateDepartPrev());
        billetDTO.setLieuDepart(voyage.getLieuDepart());
        billetDTO.setDateDepartEffectif(voyage.getDateDepartEffectif());
        billetDTO.setDateArriveEffectif(voyage.getDateArriveEffectif());
        billetDTO.setLieuArrive(voyage.getLieuArrive());
        billetDTO.setHeureDepartEffectif(voyage.getHeureDepartEffectif());
        billetDTO.setDureeVoyage(voyage.getDureeVoyage());
        billetDTO.setHeureArrive(voyage.getHeureArrive());
        billetDTO.setStatusVoyage(voyage.getStatusVoyage());
        billetDTO.setSmallImage(voyage.getSmallImage());
        billetDTO.setBigImage(voyage.getBigImage());
        billetDTO.setPointDeDepart(voyage.getPointDeDepart());
        billetDTO.setPointArrivee(voyage.getPointArrivee());

        // Informations de la classe
        billetDTO.setNomClasseVoyage(classe.getNom());
        billetDTO.setPrix(classe.getPrix());

        // Informations de l'agence
        billetDTO.setNomAgence(agence.getLongName());

        // Informations du passager
        billetDTO.setNumeroPieceIdentific(passager.getNumeroPieceIdentific());
        billetDTO.setNom(passager.getNom());
        billetDTO.setGenre(passager.getGenre() != null ? passager.getGenre().name() : null);
        billetDTO.setAge(passager.getAge());
        billetDTO.setNbrBaggage(passager.getNbrBaggage());
        billetDTO.setPlaceChoisis(passager.getPlaceChoisis());

        return billetDTO;
    }
}
