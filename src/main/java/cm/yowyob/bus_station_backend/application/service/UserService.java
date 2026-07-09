package cm.yowyob.bus_station_backend.application.service;

import cm.yowyob.bus_station_backend.application.dto.user.*;
import cm.yowyob.bus_station_backend.application.port.out.AgencePersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.NotificationPort;
import cm.yowyob.bus_station_backend.application.port.out.UserPersistencePort;
import cm.yowyob.bus_station_backend.application.port.in.UserUseCase;
import cm.yowyob.bus_station_backend.domain.enums.RoleType;
import cm.yowyob.bus_station_backend.domain.enums.StatutChauffeur;
import cm.yowyob.bus_station_backend.domain.enums.StatutEmploye;
import cm.yowyob.bus_station_backend.domain.events.UserRegisteredEvent;
import cm.yowyob.bus_station_backend.domain.exception.RegistrationException;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.factory.NotificationFactory;
import cm.yowyob.bus_station_backend.domain.model.ChauffeurAgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.EmployeAgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.User;
import cm.yowyob.bus_station_backend.application.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import cm.yowyob.bus_station_backend.infrastructure.config.security.TokenStoreService;
import org.springframework.security.authentication.BadCredentialsException;
import java.time.Duration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UserService implements UserUseCase {

    private final AgencePersistencePort agencePersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final NotificationPort notificationPort;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final UserMapper userMapper;
    private final TransactionalOperator rxtx;
    private final TokenStoreService tokenStoreService;

    public UserService(AgencePersistencePort agencePersistencePort, UserPersistencePort userPersistencePort, NotificationPort notificationPort, PasswordEncoder passwordEncoder, ApplicationEventPublisher eventPublisher, UserMapper userMapper, TransactionalOperator rxtx, TokenStoreService tokenStoreService) {
        this.agencePersistencePort = agencePersistencePort;
        this.userPersistencePort = userPersistencePort;
        this.notificationPort = notificationPort;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.userMapper = userMapper;
        this.rxtx = rxtx;
        this.tokenStoreService = tokenStoreService;
        log.info("UserService initialized. TransactionalOperator is {}", rxtx == null ? "NULL" : "PRESENT");
    }

    @Override
    public Mono<UserResponseCreatedDTO> registerUser(UserDTO userDTO) {
        return Mono.zip(
                userPersistencePort.existsByEmail(userDTO.getEmail()),
                userPersistencePort.existsByTelNumber(userDTO.getPhone_number()),
                userPersistencePort.existsByUsername(userDTO.getUsername())).flatMap(tuple -> {
                    boolean emailExists = tuple.getT1();
                    boolean phoneExists = tuple.getT2();
                    boolean userExists = tuple.getT3();

                    if (emailExists || phoneExists || userExists) {
                        HashMap<String, String> errors = new HashMap<>();
                        if (emailExists)
                            errors.put("email", "Email already exists");
                        if (phoneExists)
                            errors.put("phone_number", "Phone number already exists");
                        if (userExists)
                            errors.put("username", "Username already exists");
                        return Mono.error(new RegistrationException(HttpStatus.CONFLICT.toString(), errors));
                    }

                    User user = User.builder()
                            .nom(userDTO.getLast_name())
                            .prenom(userDTO.getFirst_name())
                            .email(userDTO.getEmail())
                            .username(userDTO.getUsername())
                            .password(passwordEncoder.encode(userDTO.getPassword()))
                            .roles(userDTO.getRole())
                            .telNumber(userDTO.getPhone_number())
                            .genre(userDTO.getGender())
                            .build();

                    return userPersistencePort.save(user);
                }).doOnSuccess(savedUser -> {
                    eventPublisher.publishEvent(new UserRegisteredEvent(savedUser));
                }).map(userMapper::toResponseCreatedDTO);
    }

    @Override
    public Mono<UserResponseDTO> getUserProfile(UUID userId) {
        return userPersistencePort.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Utilisateur non trouvé")))
                .map(userMapper::toResponseDTO);
    }

    @Override
    public Mono<UserResponseDTO> getUserProfileByUsername(String username) {
        return userPersistencePort.findByUsername(username)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Utilisateur non trouvé")))
                .map(userMapper::toResponseDTO);
    }

    @Override
    public Mono<UserResponseDTO> updateUserProfile(UUID userId, UserDTO userDTO) {
        return userPersistencePort.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Utilisateur non trouvé")))
                .flatMap(existingUser -> {
                    userMapper.updateUserFromDTO(existingUser, userDTO);
                    if (userDTO.getPassword() != null) {
                        existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                    }
                    return userPersistencePort.save(existingUser);
                })
                .map(userMapper::toResponseDTO);
    }


    @Override
    public Mono<Void> changePassword(UUID userId, String oldPassword, String newPassword) {
        return userPersistencePort.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Utilisateur non trouvé")))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                        return Mono.error(new BadCredentialsException("Ancien mot de passe incorrect"));
                    }
                    user.setPassword(passwordEncoder.encode(newPassword));
                    return userPersistencePort.save(user);
                })
                .then();
    }

    @Override
    public Mono<String> initiatePasswordReset(String email) {
        return userPersistencePort.findByEmail(email)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Aucun utilisateur trouvé pour cet email")))
                .flatMap(user -> {
                    String resetToken = UUID.randomUUID().toString();
                    return tokenStoreService.storePasswordResetToken(resetToken, user.getUsername(), Duration.ofHours(1))
                            .doOnSuccess(b -> log.info("[PWD-RESET] Token généré pour {} : {}", user.getEmail(), resetToken))
                            .thenReturn(resetToken);
                });
    }

    @Override
    public Mono<Void> resetPassword(String resetToken, String newPassword) {
        return tokenStoreService.consumePasswordResetToken(resetToken)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Token de réinitialisation invalide ou expiré")))
                .flatMap(username -> userPersistencePort.findByUsername(username)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Utilisateur non trouvé")))
                        .flatMap(user -> {
                            user.setPassword(passwordEncoder.encode(newPassword));
                            return userPersistencePort.save(user);
                        }))
                .then();
    }
    

    @Override
    public Mono<UserResponseCreatedDTO> createEmploye(EmployeRequestDTO dto, UUID agenceId, UUID currentUserId) {
        return getOrCreateUserForStaff(dto)
                .flatMap(user -> {
                    // Ajout rôle EMPLOYE
                    if (!user.getRoles().contains(RoleType.EMPLOYE)) {
                        List<RoleType> roles = new ArrayList<>(user.getRoles());
                        roles.add(RoleType.EMPLOYE);
                        user.setRoles(roles);
                    }

                    EmployeAgenceVoyage employe = userMapper.toEmployeDomainFromUserDTO(dto, agenceId, dto.getManagerId(),
                            dto.getPoste());
                    employe.setUserId(user.getUserId());

                    return Mono.zip(userPersistencePort.save(user), userPersistencePort.saveEmploye(employe))
                            .flatMap(t -> agencePersistencePort.findById(agenceId)
                                    .flatMap(agence -> notificationPort.sendNotification(
                                            NotificationFactory.createEmployeeAddedEvent(employe, t.getT1(), agence))
                                            .onErrorResume(e -> {
                                                log.error("Failed to send notification for employe {}", t.getT1().getEmail(), e);
                                                return Mono.empty();
                                            })
                                            .thenReturn(t.getT1()))
                                    .switchIfEmpty(Mono.defer(() -> {
                                        log.warn("Agence {} not found during employee creation", agenceId);
                                        return Mono.just(t.getT1());
                                    })))
                            .map(userMapper::toResponseCreatedDTO)
                            .doOnError(e -> log.error("Error creating employee", e));
                }).as(rxtx::transactional);
    }

    @Override
    public Mono<UserResponseCreatedDTO> updateEmploye(UUID employeId, EmployeRequestDTO employeDTO,
            UUID currentUserId) {
        return userPersistencePort.findEmployeById(employeId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Employé non trouvé")))
                .flatMap(existingEmploye -> userPersistencePort.findById(existingEmploye.getUserId())
                        .flatMap(existingUser -> {
                            userMapper.updateUserFromDTO(existingUser, employeDTO);
                            userMapper.updateEmployeFromDTO(existingEmploye, employeDTO);

                            if (employeDTO.getPassword() != null) {
                                existingUser.setPassword(passwordEncoder.encode(employeDTO.getPassword()));
                            }

                            return Mono.zip(userPersistencePort.save(existingUser),
                                    userPersistencePort.saveEmploye(existingEmploye))
                                    .map(t -> userMapper.toResponseCreatedDTO(t.getT1()));
                        }))
                .as(rxtx::transactional);
    }

    @Override
    public Mono<Void> deleteEmploye(UUID employeId, UUID currentUserId) {
        return userPersistencePort.findEmployeById(employeId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Employé non trouvé")))
                .flatMap(employe -> {
                    // Option 2 de l'ancien backend : Marquer comme démissionné
                    employe.setStatutEmploye(StatutEmploye.DEMISSIONNE);
                    employe.setDateFinContrat(LocalDateTime.now());
                    return userPersistencePort.saveEmploye(employe).then();
                });
    }

    @Override
    public Flux<EmployeResponseDTO> getEmployesByAgenceId(UUID agenceId) {
        return agencePersistencePort.findById(agenceId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Agence non trouvée")))
                .flatMapMany(agence -> userPersistencePort.findEmployesByAgenceId(agenceId)
                        .flatMap(employe -> userPersistencePort.findById(employe.getUserId())
                                .map(user -> userMapper.toEmployeResponseDTO(user, employe, "N/A",
                                        agence.getLongName()))));
    }

    @Override
    public Flux<EmployeResponseDTO> getEmployesByOrganisationId(UUID organisationId) {
        return null;
    }

    @Override
    public Flux<UserResponseDTO> getChauffeursByAgenceId(UUID agenceId) {
        return userPersistencePort.findChauffeursByAgenceId(agenceId)
                .flatMap(c -> userPersistencePort.findById(c.getUserId()))
                .map(userMapper::toResponseDTO);
    }

    @Override
    public Mono<Void> deleteUser(UUID userId) {
        return userPersistencePort.deleteById(userId);
    }

    @Override
    public Mono<UserResponseCreatedDTO> createChauffeur(ChauffeurRequestDTO dto, UUID agenceId, UUID currentUserId) {
        return getOrCreateUserForStaff(dto)
                .flatMap(user -> {
                    // Logique spécifique Chauffeur (est aussi un employé)
                    ChauffeurAgenceVoyage chauffeur = ChauffeurAgenceVoyage.builder()
                            .userId(user.getUserId())
                            .agenceVoyageId(agenceId)
                            .statusChauffeur(StatutChauffeur.LIBRE)
                            .build();

                    EmployeAgenceVoyage employe = userMapper.toEmployeDomainFromUserDTO(dto, agenceId, null,
                            "Chauffeur");
                    employe.setUserId(user.getUserId());

                    return Mono.zip(userPersistencePort.saveChauffeur(chauffeur),
                            userPersistencePort.saveEmploye(employe))
                            .thenReturn(user)
                            .map(userMapper::toResponseCreatedDTO)
                            .doOnError(e -> log.error("Error creating chauffeur", e));
                }).as(rxtx::transactional);
    }

    @Override
    public Mono<UserResponseCreatedDTO> updateChauffeur(UUID chauffeurId, ChauffeurRequestDTO chauffeurDTO,
            UUID currentUserId) {
        return userPersistencePort.findChauffeurById(chauffeurId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Chauffeur non trouvé")))
                .flatMap(existingChauffeur -> userPersistencePort.findById(existingChauffeur.getUserId())
                        .flatMap(existingUser -> {
                            userMapper.updateUserFromDTO(existingUser, chauffeurDTO);

                            if (chauffeurDTO.getAgenceVoyageId() != null) {
                                existingChauffeur.setAgenceVoyageId(chauffeurDTO.getAgenceVoyageId());
                            }

                            if (chauffeurDTO.getPassword() != null) {
                                existingUser.setPassword(passwordEncoder.encode(chauffeurDTO.getPassword()));
                            }

                            return Mono.zip(userPersistencePort.save(existingUser),
                                    userPersistencePort.saveChauffeur(existingChauffeur))
                                    .map(t -> userMapper.toResponseCreatedDTO(t.getT1()));
                        }))
                .as(rxtx::transactional);
    }

    @Override
    public Mono<Void> deleteChauffeur(UUID chauffeurId, UUID currentUserId) {
        return null;
    }

    // --- HELPERS PRIVÉS ---

    /**
     * Valide l'unicité des identifiants (Email, Tel, Username)
     */
    private Mono<Void> validateUserUniqueness(String email, String phone, String username) {
        return Mono.zip(
                userPersistencePort.existsByEmail(email),
                userPersistencePort.existsByTelNumber(phone),
                userPersistencePort.existsByUsername(username)).flatMap(tuple -> {
                    if (tuple.getT1() || tuple.getT2() || tuple.getT3()) {
                        HashMap<String, String> errors = new HashMap<>();
                        if (tuple.getT1())
                            errors.put("email", "Email already exists");
                        if (tuple.getT2())
                            errors.put("phone_number", "Phone number already exists");
                        if (tuple.getT3())
                            errors.put("username", "Username already exists");
                        return Mono.error(new RegistrationException(HttpStatus.CONFLICT.toString(), errors));
                    }
                    return Mono.empty();
                });
    }

    /**
     * Récupère un utilisateur existant par email ou en crée un nouveau si demandé
     */
    private Mono<User> getOrCreateUserForStaff(UserDTO dto) {
        return userPersistencePort.findByEmail(dto.getEmail())
                .onErrorResume(e -> Mono.empty())
                .flatMap(existing -> {
                    log.info("Utilisateur existant trouvé pour le staff : {}", existing.getEmail());
                    return Mono.just(existing);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Création d'un nouvel utilisateur pour le staff");
                    User newUser = userMapper.toDomain(dto);
                    newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
                    return userPersistencePort.save(newUser);
                }));
    }
}