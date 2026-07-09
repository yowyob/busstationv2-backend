package cm.yowyob.bus_station_backend.infrastructure.inbound.handler;

import cm.yowyob.bus_station_backend.domain.exception.*;
import cm.yowyob.bus_station_backend.infrastructure.inbound.error.ApiError;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.dao.DataIntegrityViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import org.springframework.security.access.AccessDeniedException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ApiError>> handleNotFound(ResourceNotFoundException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND, ex.getMessage())));
    }

    @ExceptionHandler(UnauthorizeException.class)
    public Mono<ResponseEntity<ApiError>> handleForbidden(UnauthorizeException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(HttpStatus.FORBIDDEN, ex.getMessage())));
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public Mono<ResponseEntity<ApiError>> handleBusiness(BusinessRuleViolationException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(HttpStatus.CONFLICT, ex.getMessage())));
    }

    @ExceptionHandler(ReservationException.class)
    public Mono<ResponseEntity<ApiError>> handleReservation(ReservationException ex) {
        return Mono.just(ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST, ex.getMessage())));
    }

    @ExceptionHandler(RegistrationException.class)
    public Mono<ResponseEntity<ApiError>> handleRegistration(RegistrationException ex) {
        return Mono.just(ResponseEntity.badRequest()
                .body(ApiError.of(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        ex.getErrors())));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Mono<ResponseEntity<ApiError>> handleConflict(Exception ex) {
        log.error("Conflit de données : {}", ex.getMessage());
        ApiError error = new ApiError(409, "Conflit", "Une contrainte d'intégrité a été violée", null);
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(error));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ApiError>> handleResponseStatus(ResponseStatusException ex) {

        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return Mono.just(
                ResponseEntity
                        .status(status)
                        .body(ApiError.of(
                                status,
                                ex.getReason())));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Mono<ResponseEntity<ApiError>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return Mono.just(
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiError.of(HttpStatus.UNAUTHORIZED, ex.getMessage())));
    }
    
    @ExceptionHandler(org.springframework.core.codec.DecodingException.class)
    public Mono<ResponseEntity<ApiError>> handleDecodingException(org.springframework.core.codec.DecodingException ex) {
        log.warn("JSON decoding failed: {}", ex.getMessage(), ex);
        String detail = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        return Mono.just(ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST,
                        "Le corps de la requête est invalide : " + detail)));
    }

    @ExceptionHandler(org.springframework.web.bind.support.WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiError>> handleValidation(
            org.springframework.web.bind.support.WebExchangeBindException ex) {
        java.util.Map<String, String> errors = new java.util.HashMap<>();
        ex.getFieldErrors().forEach(err -> errors.put(err.getField(),
                err.getDefaultMessage() != null ? err.getDefaultMessage() : "invalide"));
        log.warn("Validation failed: {}", errors);
        return Mono.just(ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "Validation failure", errors)));
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ApiError>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(ApiError.of(
                                HttpStatus.FORBIDDEN,
                                ex.getMessage() != null ? ex.getMessage() : "Accès refusé")));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiError>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiError.of(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Erreur interne du serveur")));
    }
}

