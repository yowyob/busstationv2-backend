package cm.yowyob.bus_station_backend.application.port.out;

import cm.yowyob.bus_station_backend.domain.events.NotificationEvent;
import reactor.core.publisher.Mono;

public interface NotificationPort {
    // Envoie l'événement dans Kafka
    Mono<Void> sendNotification(NotificationEvent event);
}
