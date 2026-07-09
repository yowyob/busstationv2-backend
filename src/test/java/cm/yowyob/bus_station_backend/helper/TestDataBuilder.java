package cm.yowyob.bus_station_backend.helper;

import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageDTO;
import cm.yowyob.bus_station_backend.application.dto.user.UserDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageCreateRequestDTO;
import cm.yowyob.bus_station_backend.domain.enums.*;

import java.time.LocalDateTime;
import java.util.*;

public class TestDataBuilder {

    public static UserDTO createTestUser() {
        UserDTO user = new UserDTO();
        user.setLast_name("Dupont");
        user.setFirst_name("Jean");
        user.setEmail("jean.dupont." + UUID.randomUUID().toString().substring(0, 5) + "@test.com");
        user.setPassword("Password123!");
        user.setPhone_number("+237123456789");
        user.setGender(Gender.MALE);
        return user;
    }

    public static AgenceVoyageDTO createTestAgence(UUID organizationId, UUID userId, UUID gareRoutiereId) {
        AgenceVoyageDTO agence = new AgenceVoyageDTO();
        agence.setOrganisation_id(organizationId);
        agence.setUser_id(userId);
        agence.setLong_name("Agence Test Transport");
        agence.setShort_name("ATT-" + UUID.randomUUID().toString().substring(0, 5));
        agence.setLocation("Yaoundé");
        agence.setGare_routiere_id(gareRoutiereId);
        agence.setSocial_network("@agence_test");
        agence.setDescription("Une agence de test pour les voyages");
        agence.setGreeting_message("Bienvenue chez nous!");
        return agence;
    }

    public static VoyageCreateRequestDTO createTestVoyage() {
        VoyageCreateRequestDTO voyage = new VoyageCreateRequestDTO();
        voyage.setTitre("Yaoundé - Douala Express");
        voyage.setDescription("Voyage direct et confortable");
        voyage.setDateDepartPrev(LocalDateTime.now().plusDays(1)); // Demain
        voyage.setLieuDepart("Yaoundé");
        voyage.setLieuArrive("Douala");
        voyage.setPointDeDepart("Gare centrale Yaoundé");
        voyage.setPointArrivee("Gare Bonabéri");
        voyage.setNbrPlaceReservable(50);
        voyage.setNbrPlaceRestante(50);
        voyage.setDateLimiteReservation(LocalDateTime.now().plusHours(12)); // 12h
        voyage.setDateLimiteConfirmation(LocalDateTime.now().plusHours(20)); // 20h
        voyage.setStatusVoyage(StatutVoyage.PUBLIE);
        voyage.setAmenities(List.of(Amenities.AC, Amenities.WIFI));
        return voyage;
    }
}