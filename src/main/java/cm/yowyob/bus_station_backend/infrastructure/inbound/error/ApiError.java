package cm.yowyob.bus_station_backend.infrastructure.inbound.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ApiError {

    private final int status;
    private final String error;
    private final String message;
    private final Object details;

    public static ApiError of(HttpStatus status, String message) {
        return new ApiError(
                status.value(),
                status.getReasonPhrase(),
                message,
                null
        );
    }

    public static ApiError of(HttpStatus status, String message, Object details) {
        return new ApiError(
                status.value(),
                status.getReasonPhrase(),
                message,
                details
        );
    }
}

