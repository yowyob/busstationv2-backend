package cm.yowyob.bus_station_backend.domain.events;

import cm.yowyob.bus_station_backend.domain.enums.NotificationType;
import cm.yowyob.bus_station_backend.domain.enums.RecipientType;

import lombok.Builder;
import lombok.Data;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class NotificationEvent {
    private NotificationType type;
    private RecipientType recipientType;
    private UUID recipientId;
    private Map<String, Object> variables;
}