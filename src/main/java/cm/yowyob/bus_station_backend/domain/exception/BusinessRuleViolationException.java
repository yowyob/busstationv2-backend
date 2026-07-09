package cm.yowyob.bus_station_backend.domain.exception;

// Pour les règles métier non respectées
public class BusinessRuleViolationException extends BusStationException{
    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
