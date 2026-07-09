package cm.yowyob.bus_station_backend.domain.exception;

public class ResourceNotFoundException extends BusStationException {
    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(String.format("%s introuvable avec l'identifiant : %s", resourceName, identifier));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
