package cm.yowyob.bus_station_backend.application.mapper;

import cm.yowyob.bus_station_backend.application.dto.user.*;
import cm.yowyob.bus_station_backend.domain.enums.StatutEmploye;
import cm.yowyob.bus_station_backend.domain.model.EmployeAgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class UserMapper {

    // Formatters pour les dates
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * Conversion de ChauffeurRequestDTO vers User (domaine)
     * ChauffeurRequestDTO étend UserDTO et ajoute agenceVoyageId et isUserExist
     */
    public User toDomainFromChauffeurRequest(ChauffeurRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return User.builder()
                .nom(dto.getLast_name())
                .prenom(dto.getFirst_name())
                .email(dto.getEmail())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .telNumber(dto.getPhone_number())
                .roles(dto.getRole())
                .genre(dto.getGender())
                .build();
    }

    /**
     * Mappe les champs communs de UserDTO vers le domaine User.
     * Valable pour UserDTO, ChauffeurRequestDTO et EmployeRequestDTO.
     */
    public User toDomain(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        return User.builder()
                .nom(dto.getLast_name())
                .prenom(dto.getFirst_name())
                .email(dto.getEmail())
                .username(dto.getUsername())
                .password(dto.getPassword()) // Le hashage doit se faire dans le Service, pas le mapper
                .telNumber(dto.getPhone_number())
                .roles(dto.getRole())
                .genre(dto.getGender())
                .build();
    }

    public UserResponseDTO toResponseDTO(User domain) {
        if (domain == null) {
            return null;
        }

        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(domain.getUserId());
        dto.setLastName(domain.getNom());
        dto.setFirstName(domain.getPrenom());
        dto.setEmail(domain.getEmail());
        dto.setUsername(domain.getUsername());
        dto.setPhoneNumber(domain.getTelNumber());
        dto.setRole(domain.getRoles() != null ? domain.getRoles() : new ArrayList<>());
        // Token n'est pas dans le modèle User, il est ajouté par le contrôleur/service
        // d'authentification
        return dto;
    }

    public UserResponseDTO toResponseDTO(User domain, String token) {
        if (domain == null) {
            return null;
        }

        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(domain.getUserId());
        dto.setLastName(domain.getNom());
        dto.setFirstName(domain.getPrenom());
        dto.setEmail(domain.getEmail());
        dto.setUsername(domain.getUsername());
        dto.setPhoneNumber(domain.getTelNumber());
        dto.setRole(domain.getRoles() != null ? domain.getRoles() : new ArrayList<>());
        dto.setToken(token);
        return dto;
    }

    // Note: Pour EmployeResponseDTO, il faut des données qui ne sont pas dans User
    // (StatutEmploye, AgenceId).
    // Ce mapping doit probablement être fait manuellement dans le Service en
    // agrégeant User + EmployeEntity.

    /**
     * Conversion de User (domaine) vers UserResponseCreatedDTO
     * Note: Certains champs nécessitent des valeurs par défaut ou des conversions
     * spécifiques
     */
    public UserResponseCreatedDTO toResponseCreatedDTO(User domain) {
        if (domain == null) {
            return null;
        }

        // Formatage des dates (si disponibles)
        ZonedDateTime now = ZonedDateTime.now();
        String currentTime = now.format(FORMATTER);

        return UserResponseCreatedDTO.builder()
                .id(domain.getUserId() != null ? domain.getUserId().toString() : null)
                .email(domain.getEmail())
                .username(domain.getUsername())
                .first_name(domain.getPrenom())
                .last_name(domain.getNom())
                .phone_number(domain.getTelNumber())
                .gender(domain.getGenre())
                .roles(domain.getRoles())
                .type(domain.getBusinessActorType())
                .friendly_name(domain.getNom() + " " + domain.getPrenom())
                // Dates (peuvent être personnalisées selon votre logique)
                .created_at(currentTime)
                .updated_at(currentTime)
                .registration_date(currentTime)
                // Valeurs par défaut pour les champs optionnels
                .keywords(List.of(domain.getEmail(), domain.getUsername()))
                .build();
    }

    /**
     * Conversion de User (domaine) et Employe (domaine) vers EmployeResponseDTO
     * Requiert les deux entités car EmployeResponseDTO contient des données des
     * deux
     */
    public EmployeResponseDTO toEmployeResponseDTO(User user, EmployeAgenceVoyage employe, String nomManager,
            String nomAgence) {
        if (user == null || employe == null) {
            return null;
        }

        EmployeResponseDTO dto = new EmployeResponseDTO();
        dto.setEmployeId(employe.getEmployeId());
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getPrenom());
        dto.setLastName(user.getNom());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getTelNumber());
        dto.setPoste(employe.getPoste());
        dto.setDepartement(employe.getDepartement());
        dto.setDateEmbauche(employe.getDateEmbauche());
        dto.setStatutEmploye(employe.getStatutEmploye());
        dto.setNomManager(nomManager); // Doit être fourni depuis le service
        dto.setAgenceVoyageId(employe.getAgenceVoyageId());
        dto.setNomAgence(nomAgence); // Doit être fourni depuis le service

        return dto;
    }

    /**
     * Conversion de User (domaine) vers EmployeResponseDTO (version simplifiée)
     * Quand on a seulement les informations utilisateur
     */
    public EmployeResponseDTO toEmployeResponseDTOFromUser(User user, UUID agenceVoyageId, String nomAgence) {
        if (user == null) {
            return null;
        }

        EmployeResponseDTO dto = new EmployeResponseDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getPrenom());
        dto.setLastName(user.getNom());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getTelNumber());
        dto.setAgenceVoyageId(agenceVoyageId);
        dto.setNomAgence(nomAgence);
        // Les autres champs (poste, département, etc.) restent null ou vides

        return dto;
    }

    /**
     * Conversion de ChauffeurRequestDTO vers Employe (domaine)
     * Pour créer un employé/chauffeur à partir d'une demande
     */
    public EmployeAgenceVoyage toEmployeDomainFromChauffeurRequest(ChauffeurRequestDTO dto, UUID managerId) {
        if (dto == null) {
            return null;
        }

        return EmployeAgenceVoyage.builder()
                .agenceVoyageId(dto.getAgenceVoyageId())
                .poste("Chauffeur") // Poste par défaut pour un chauffeur
                .statutEmploye(StatutEmploye.ACTIF)
                .dateEmbauche(LocalDateTime.now())
                .managerId(managerId)
                .build();
    }

    /**
     * Conversion de UserDTO vers Employe (domaine) - version générique
     */
    public EmployeAgenceVoyage toEmployeDomainFromUserDTO(UserDTO dto, UUID agenceId, UUID managerId, String poste) {
        if (dto == null) {
            return null;
        }

        return EmployeAgenceVoyage.builder()
                .agenceVoyageId(agenceId)
                .poste(poste)
                .departement("Transport") // Département par défaut
                .statutEmploye(StatutEmploye.ACTIF)
                .dateEmbauche(LocalDateTime.now())
                .managerId(managerId)
                .build();
    }

    /**
     * Mise à jour partielle d'un User existant à partir d'un UserDTO
     */
    public void updateUserFromDTO(User existingUser, UserDTO dto) {
        if (existingUser == null || dto == null) {
            return;
        }

        if (dto.getLast_name() != null) {
            existingUser.setNom(dto.getLast_name());
        }
        if (dto.getFirst_name() != null) {
            existingUser.setPrenom(dto.getFirst_name());
        }
        if (dto.getEmail() != null) {
            existingUser.setEmail(dto.getEmail());
        }
        if (dto.getUsername() != null) {
            existingUser.setUsername(dto.getUsername());
        }
        if (dto.getPassword() != null) {
            existingUser.setPassword(dto.getPassword());
        }
        if (dto.getPhone_number() != null) {
            existingUser.setTelNumber(dto.getPhone_number());
        }
        if (dto.getRole() != null) {
            existingUser.setRoles(dto.getRole());
        }
        if (dto.getGender() != null) {
            existingUser.setGenre(dto.getGender());
        }
    }

    /**
     * Mise à jour d'un employé existant à partir d'un EmployeRequestDTO
     */
    public void updateEmployeFromDTO(EmployeAgenceVoyage existing, EmployeRequestDTO dto) {
        if (existing == null || dto == null) {
            return;
        }

        if (dto.getPoste() != null) {
            existing.setPoste(dto.getPoste());
        }
        if (dto.getDepartement() != null) {
            existing.setDepartement(dto.getDepartement());
        }
        if (dto.getSalaire() != null) {
            existing.setSalaire(dto.getSalaire());
        }
        if (dto.getManagerId() != null) {
            existing.setManagerId(dto.getManagerId());
        }
        if (dto.getAgenceVoyageId() != null) {
            existing.setAgenceVoyageId(dto.getAgenceVoyageId());
        }
    }

    /**
     * Conversion d'une liste d'utilisateurs vers liste de DTOs
     */
    public List<UserResponseDTO> toResponseDTOList(List<User> users) {
        if (users == null) {
            return new ArrayList<>();
        }

        return users.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Conversion d'une liste d'employés (User + Employe) vers liste de
     * EmployeResponseDTO
     */
    public List<EmployeResponseDTO> toEmployeResponseDTOList(
            List<User> users,
            List<EmployeAgenceVoyage> employes,
            List<String> nomsManagers,
            List<String> nomsAgences) {

        if (users == null || employes == null ||
                users.size() != employes.size() ||
                (nomsManagers != null && users.size() != nomsManagers.size()) ||
                (nomsAgences != null && users.size() != nomsAgences.size())) {
            return new ArrayList<>();
        }

        List<EmployeResponseDTO> result = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            String nomManager = nomsManagers != null ? nomsManagers.get(i) : null;
            String nomAgence = nomsAgences != null ? nomsAgences.get(i) : null;

            EmployeResponseDTO dto = toEmployeResponseDTO(
                    users.get(i),
                    employes.get(i),
                    nomManager,
                    nomAgence);
            result.add(dto);
        }

        return result;
    }

}