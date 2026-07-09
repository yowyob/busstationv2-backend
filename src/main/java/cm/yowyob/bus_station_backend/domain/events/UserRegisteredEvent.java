package cm.yowyob.bus_station_backend.domain.events;

import cm.yowyob.bus_station_backend.domain.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Événement déclenché lorsqu'un utilisateur s'inscrit avec succès.
 * Cet événement est indépendant de Spring Framework (POJO pur) pour respecter
 * le DDD,
 * mais sera enveloppé par le publisher de Spring dans la couche Application.
 */
@Getter
@RequiredArgsConstructor
public class UserRegisteredEvent {
    private final User user;
    private final LocalDateTime occurredOn = LocalDateTime.now();
}
