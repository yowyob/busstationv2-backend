package cm.yowyob.bus_station_backend.domain.enums.planning;

public enum StatutPlanning {
    BROUILLON,  // Draft - being edited
    ACTIF,      // Active - published and visible
    INACTIF,    // Inactive - temporarily disabled
    ARCHIVE     // Archived - no longer in use
}
