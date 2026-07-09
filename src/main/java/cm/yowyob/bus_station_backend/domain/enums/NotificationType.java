package cm.yowyob.bus_station_backend.domain.enums;

public enum NotificationType {
    // Voyage related
    VOYAGE_CREATED,
    VOYAGE_UPDATED,
    VOYAGE_CANCELLED,
    VOYAGE_PUBLISHED,

    // Reservation related
    RESERVATION_CREATED,
    RESERVATION_CONFIRMED,
    RESERVATION_CANCELLED,
    RESERVATION_EXPIRED,

    // Payment related
    PAYMENT_RECEIVED,
    PAYMENT_FAILED,
    PAYMENT_PENDING,

    // User related
    USER_REGISTERED,
    USER_PROFILE_UPDATED,
    PASSWORD_RESET,

    // Agency related
    AGENCY_CREATED,
    AGENCY_UPDATED,

    // Driver related
    DRIVER_ASSIGNED,
    DRIVER_UNASSIGNED,

    // Employee related
    EMPLOYEE_ADDED,
    EMPLOYEE_UPDATED,
    EMPLOYEE_REMOVED,

    // Organization related
    ORGANIZATION_CREATED,
    ORGANIZATION_UPDATED,

    // System related
    SYSTEM_MAINTENANCE,
    SYSTEM_UPDATE
}
