package cm.yowyob.bus_station_backend.domain.factory;

import cm.yowyob.bus_station_backend.application.dto.reservation.ReservationCancelByAgenceDTO;
import cm.yowyob.bus_station_backend.domain.enums.NotificationType;
import cm.yowyob.bus_station_backend.domain.enums.RecipientType;
import cm.yowyob.bus_station_backend.domain.events.NotificationEvent;
import cm.yowyob.bus_station_backend.domain.model.*;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class NotificationFactory {

        public static NotificationEvent createVoyageCreatedEvent(Voyage voyage, UUID chiefAgenceId) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("voyageTitle", voyage.getTitre());
                variables.put("voyageDate", voyage.getDateDepartPrev());
                variables.put("voyageDestination", voyage.getLieuArrive());
                variables.put("voyageDescription", voyage.getDescription());
                variables.put("departureLocation", voyage.getLieuDepart());

                return NotificationEvent.builder()
                                .type(NotificationType.VOYAGE_CREATED)
                                .recipientType(RecipientType.AGENCY)
                                .recipientId(chiefAgenceId)
                                .variables(variables)
                                .build();
        }

        public static NotificationEvent createVoyageCancelledEvent(Voyage voyage, UUID recipientId,
                        RecipientType recipientType) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("voyageTitle", voyage.getTitre());
                variables.put("voyageDate", voyage.getDateDepartPrev());
                variables.put("voyageDestination", voyage.getLieuArrive());
                variables.put("departureLocation", voyage.getLieuDepart());

                return NotificationEvent.builder()
                                .type(NotificationType.VOYAGE_CANCELLED)
                                .recipientType(recipientType)
                                .recipientId(recipientId)
                                .variables(variables)
                                .build();
        }

        public static NotificationEvent createReservationConfirmedEvent(Reservation reservation, Voyage voyage) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("reservationId", reservation.getIdReservation().toString());
                variables.put("passengerCount", reservation.getNbrPassager());
                variables.put("totalAmount", reservation.getPrixTotal());
                variables.put("voyageTitle", voyage.getTitre());
                variables.put("voyageDate", voyage.getDateDepartPrev());

                return NotificationEvent.builder()
                                .type(NotificationType.RESERVATION_CONFIRMED)
                                .recipientType(RecipientType.USER)
                                .recipientId(reservation.getIdUser())
                                .variables(variables)
                                .build();
        }

        public static NotificationEvent createReservationCancelledEvent(Reservation reservation) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("reservationId", reservation.getIdReservation().toString());
                variables.put("passengerCount", reservation.getNbrPassager());
                variables.put("refundAmount", reservation.getMontantPaye());

                return NotificationEvent.builder()
                                .type(NotificationType.RESERVATION_CANCELLED)
                                .recipientType(RecipientType.USER)
                                .recipientId(reservation.getIdUser())
                                .variables(variables)
                                .build();
        }

        public static NotificationEvent createReservationCancelledByAgencyEvent(Reservation reservation,
                        ReservationCancelByAgenceDTO cancelDTO) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("reservationId", reservation.getIdReservation().toString());
                variables.put("passengerCount", reservation.getNbrPassager());
                variables.put("refundAmount", reservation.getMontantPaye());
                variables.put("reason", cancelDTO.getCauseAnnulation());

                return NotificationEvent.builder()
                                .type(NotificationType.RESERVATION_CANCELLED)
                                .recipientType(RecipientType.USER)
                                .recipientId(reservation.getIdUser())
                                .variables(variables)
                                .build();
        }

        public static NotificationEvent createPaymentReceivedEvent(Reservation reservation) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("reservationId", reservation.getIdReservation().toString());
                variables.put("amount", reservation.getMontantPaye());
                variables.put("transactionCode", reservation.getTransactionCode());

                return NotificationEvent.builder()
                                .type(NotificationType.PAYMENT_RECEIVED)
                                .recipientType(RecipientType.USER)
                                .recipientId(reservation.getIdUser())
                                .variables(variables)
                                .build();
        }

        public static NotificationEvent createPaymentFailedEvent(Reservation reservation) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("reservationId", reservation.getIdReservation().toString());
                variables.put("amount", reservation.getPrixTotal());
                variables.put("transactionCode", reservation.getTransactionCode());

                return NotificationEvent.builder()
                                .type(NotificationType.PAYMENT_FAILED)
                                .recipientType(RecipientType.USER)
                                .recipientId(reservation.getIdUser())
                                .variables(variables)
                                .build();
        }

        public static NotificationEvent createUserRegisteredEvent(User user) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("userName", user.getPrenom() + " " + user.getNom());
                variables.put("userEmail", user.getEmail());
                variables.put("welcomeMessage", "Bienvenue ! Découvrez nos services de voyage.");

                return NotificationEvent.builder()
                                .type(NotificationType.USER_REGISTERED)
                                .recipientType(RecipientType.USER)
                                .recipientId(user.getUserId())
                                .variables(variables)
                                .build();
        }

        public static NotificationEvent createAgencyCreatedEvent(AgenceVoyage agence, User manager) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("agencyName", agence.getLongName());
                variables.put("agencyShortName", agence.getShortName());
                variables.put("managerName", manager.getPrenom() + " " + manager.getNom());
                variables.put("location", agence.getLocation());

                return NotificationEvent.builder()
                                .type(NotificationType.AGENCY_CREATED)
                                .recipientType(RecipientType.AGENCY)
                                .recipientId(agence.getAgencyId())
                                .variables(variables)
                                .build();
        }

        public static NotificationEvent createDriverAssignedEvent(UUID driverId, Voyage voyage) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("voyageTitle", voyage.getTitre());
                variables.put("voyageDate", voyage.getDateDepartPrev());
                variables.put("departure", voyage.getLieuDepart());
                variables.put("destination", voyage.getLieuArrive());
                variables.put("departurePoint", voyage.getPointDeDepart());
                variables.put("arrivalPoint", voyage.getPointArrivee());

                return NotificationEvent.builder()
                                .type(NotificationType.DRIVER_ASSIGNED)
                                .recipientType(RecipientType.DRIVER)
                                .recipientId(driverId)
                                .variables(variables)
                                .build();
        }

        public static NotificationEvent createEmployeeAddedEvent(EmployeAgenceVoyage employe, User user,
                        AgenceVoyage agence) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("employeeName", user.getPrenom() + " " + user.getNom());
                variables.put("poste", employe.getPoste());
                variables.put("departement", employe.getDepartement());
                variables.put("agencyName", agence.getLongName());
                variables.put("dateEmbauche", employe.getDateEmbauche());

                return NotificationEvent.builder()
                                .type(NotificationType.EMPLOYEE_ADDED)
                                .recipientType(RecipientType.EMPLOYEE)
                                .recipientId(user.getUserId())
                                .variables(variables)
                                .build();
        }

        public static NotificationEvent createReservationCreatedEvent(Reservation reservation, Voyage voyage,
                        UUID agenceId) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("reservationId", reservation.getIdReservation().toString());
                variables.put("voyageTitle", voyage.getTitre());
                variables.put("passengerCount", reservation.getNbrPassager());
                variables.put("totalPayed", reservation.getMontantPaye());
                variables.put("totalAmount", reservation.getPrixTotal());
                variables.put("reservationDate", reservation.getDateReservation());

                return NotificationEvent.builder()
                                .type(NotificationType.RESERVATION_CREATED)
                                .recipientType(RecipientType.AGENCY)
                                .recipientId(agenceId)
                                .variables(variables)
                                .build();
        }

        public static List<NotificationEvent> createMultipleUserEvents(NotificationType type,
                        List<UUID> userIds,
                        Map<String, Object> commonVariables) {
                return userIds.stream()
                                .map(userId -> NotificationEvent.builder()
                                                .type(type)
                                                .recipientType(RecipientType.USER)
                                                .recipientId(userId)
                                                .variables(new HashMap<>(commonVariables))
                                                .build())
                                .collect(Collectors.toList());
        }
}