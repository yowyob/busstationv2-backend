package cm.yowyob.bus_station_backend.infrastructure.outbound.kafka;

import cm.yowyob.bus_station_backend.application.port.out.NotificationPort;
import cm.yowyob.bus_station_backend.domain.events.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class KafkaNotificationAdapter implements NotificationPort {

        @Autowired(required = false)
        @Nullable
        private KafkaTemplate<String, Object> kafkaTemplate;

        // Topic défini dans application.properties ou en dur ici
        private static final String TOPIC_NOTIFICATIONS = "bus-station.notifications";

        @Override
        @Async
        public Mono<Void> sendNotification(NotificationEvent event) {
                log.debug("Préparation de l'envoi Kafka pour l'événement: {}", event.getType());

                // KafkaTemplate.send retourne un CompletableFuture (depuis Spring Boot 3) ou
                // ListenableFuture (avant)
                // Mono.fromFuture permet d'intégrer ça dans le flux réactif
                // CompletableFuture<SendResult<String, Object>> future =
                // kafkaTemplate.send(TOPIC_NOTIFICATIONS,
                // event.getType().name(), event);

                // return Mono.fromFuture(future)
                // .doOnSuccess(result -> log.info("Notification envoyée à Kafka [Topic: {},
                // Offset: {}]",
                // TOPIC_NOTIFICATIONS, result.getRecordMetadata().offset()))
                // .doOnError(ex -> log.error("Échec de l'envoi Kafka pour l'événement {}",
                // event.getType(), ex))
                // .then(); // On retourne Mono<Void> car le résultat précis importe peu au
                // métier
                return Mono.empty();
        }
}
