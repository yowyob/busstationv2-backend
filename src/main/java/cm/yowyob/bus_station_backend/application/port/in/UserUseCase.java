package cm.yowyob.bus_station_backend.application.port.in;

import cm.yowyob.bus_station_backend.application.dto.user.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserUseCase {
    // Authentification & Inscription
    Mono<UserResponseCreatedDTO> registerUser(UserDTO userDTO);

    Mono<UserResponseDTO> getUserProfile(UUID userId);

    Mono<UserResponseDTO> getUserProfileByUsername(String username);

    Mono<UserResponseDTO> updateUserProfile(UUID userId, UserDTO userDTO);

    Mono<Void> deleteUser(UUID userId);

    // Gestion du mot de passe
    Mono<Void> changePassword(UUID userId, String oldPassword, String newPassword);

    Mono<String> initiatePasswordReset(String email);

    Mono<Void> resetPassword(String resetToken, String newPassword);

    // Gestion des Employés
    Mono<UserResponseCreatedDTO> createEmploye(EmployeRequestDTO employeDTO, UUID agenceId, UUID currentUserId);

    Mono<UserResponseCreatedDTO> updateEmploye(UUID employeId, EmployeRequestDTO employeDTO, UUID currentUserId);

    Mono<Void> deleteEmploye(UUID employeId, UUID currentUserId);

    Flux<EmployeResponseDTO> getEmployesByAgenceId(UUID agenceId);

    Flux<EmployeResponseDTO> getEmployesByOrganisationId(UUID organisationId);

    // Gestion des Chauffeurs
    Mono<UserResponseCreatedDTO> createChauffeur(ChauffeurRequestDTO chauffeurDTO, UUID agenceId, UUID currentUserId);

    Mono<UserResponseCreatedDTO> updateChauffeur(UUID chauffeurId, ChauffeurRequestDTO chauffeurDTO,
            UUID currentUserId);

    Mono<Void> deleteChauffeur(UUID chauffeurId, UUID currentUserId);

    Flux<UserResponseDTO> getChauffeursByAgenceId(UUID agenceId);

}