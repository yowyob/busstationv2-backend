package cm.yowyob.bus_station_backend.domain.exception;

import lombok.Getter;

@Getter
public class BusStationException extends RuntimeException {
    private final String message;

    protected BusStationException(String message) {
        super(message);
        this.message = message;
    }

    protected BusStationException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
}
