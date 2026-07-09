package cm.yowyob.bus_station_backend.domain.events;

import cm.yowyob.bus_station_backend.domain.model.AgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.EmployeAgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class EmployeeAddedEvent {
    private final EmployeAgenceVoyage employeInfo;
    private final User userInfo;
    private final AgenceVoyage agence;
    private final LocalDateTime occurredOn = LocalDateTime.now();
}
