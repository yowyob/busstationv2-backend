package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.enums.RoleType;
import cm.yowyob.bus_station_backend.domain.model.User;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserPersistenceMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) return null;

        return User.builder()
                .userId(entity.getUserId())
                .nom(entity.getNom())
                .prenom(entity.getPrenom())
                .genre(entity.getGenre())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .telNumber(entity.getTelNumber())
                .roles(parseRoles(entity.getRoles()))
                .businessActorType(entity.getBusinessActorType())
                .address(entity.getAddress())
                .idcoordonneeGPS(entity.getIdcoordonneeGPS())
                .build();
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) return null;

        return UserEntity.builder()
                .userId(domain.getUserId())
                .nom(domain.getNom())
                .prenom(domain.getPrenom())
                .genre(domain.getGenre())
                .username(domain.getUsername())
                .email(domain.getEmail())
                .password(domain.getPassword())
                .telNumber(domain.getTelNumber())
                .roles(formatRoles(domain.getRoles()))
                .businessActorType(domain.getBusinessActorType())
                .address(domain.getAddress())
                .idcoordonneeGPS(domain.getIdcoordonneeGPS())
                .build();
    }

    private List<RoleType> parseRoles(String roles) {
        if (roles == null || roles.isBlank()) return List.of();
        return Arrays.stream(roles.split(","))
                .map(RoleType::valueOf)
                .toList();
    }

    private String formatRoles(List<RoleType> roles) {
        if (roles == null || roles.isEmpty()) return "";
        return roles.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }
}

