package cm.yowyob.bus_station_backend.domain.exception;

import lombok.Getter;
import java.util.Map;

/**
 * Exception métier levée lors de problèmes de validation à l'inscription.
 * Elle se trouve dans le Domaine car elle représente une règle métier violée.
 */
@Getter
public class RegistrationException extends BusStationException {

    private final Map<String, String> errors;

    public RegistrationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

    public RegistrationException(Map<String, String> errors) {
        super("Échec de l'enregistrement : des conflits de données existent.");
        this.errors = errors;
    }
}