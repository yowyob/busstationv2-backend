package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.VoyageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.UUID;
public interface VoyageR2dbcRepository extends R2dbcRepository<VoyageEntity, UUID> {
    @Query("""
        SELECT * FROM voyages
        ORDER BY date_publication DESC
        LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
    """)
    Flux<VoyageEntity> findAllPaged(Pageable pageable);

    @Query("""
        SELECT v.* FROM voyages v
        JOIN lignes_voyage l ON l.id_voyage = v.id_voyage
        WHERE l.id_agence_voyage = :agenceId
        ORDER BY v.date_publication DESC
        LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
    """)
    Flux<VoyageEntity> findByAgenceIdPaged(UUID agenceId, Pageable pageable);

    @Query("SELECT COUNT(*) FROM voyages v JOIN lignes_voyage l ON l.id_voyage = v.id_voyage WHERE l.id_agence_voyage = :agenceId")
    Mono<Long> countByAgenceId(UUID agenceId);

    @Query("""
        SELECT * FROM voyages
        WHERE point_de_depart ILIKE '%' || :pointName || '%'
           OR point_arrivee ILIKE '%' || :pointName || '%'
        ORDER BY date_publication DESC
        LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
    """)
    Flux<VoyageEntity> findByPointNamePaged(String pointName, Pageable pageable);

    @Query("""
        SELECT COUNT(*) FROM voyages
        WHERE point_de_depart ILIKE '%' || :pointName || '%'
           OR point_arrivee ILIKE '%' || :pointName || '%'
    """)
    Mono<Long> countByPointName(String pointName);

    @Query("""
        SELECT p.place_choisis
        FROM passagers p
        JOIN reservations r ON r.id_reservation = p.id_reservation
        WHERE r.id_voyage = :voyageId
          AND r.statut_reservation <> 'ANNULER'
    """)
    Flux<Integer> findOccupiedPlacesByVoyageId(UUID voyageId);

// LOT 3 — nouveaux (avec JOIN sur lignes_voyage)
    @Query("""
        SELECT v.* FROM voyages v
        JOIN lignes_voyage l ON l.id_voyage = v.id_voyage
        WHERE v.status_voyage = 'PUBLIE'
          AND l.id_agence_voyage = :agenceId
          AND v.date_depart_prev > :now
        ORDER BY v.date_depart_prev ASC
        LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
    """)
    Flux<VoyageEntity> findPublicByAgenceId(UUID agenceId, LocalDateTime now, Pageable pageable);

    @Query("""
        SELECT COUNT(*) FROM voyages v
        JOIN lignes_voyage l ON l.id_voyage = v.id_voyage
        WHERE v.status_voyage = 'PUBLIE' AND l.id_agence_voyage = :agenceId AND v.date_depart_prev > :now
    """)
    Mono<Long> countPublicByAgenceId(UUID agenceId, LocalDateTime now);

    @Query("""
        SELECT * FROM voyages
        WHERE status_voyage = 'PUBLIE'
          AND date_depart_prev > :now
          AND id_voyage != :excludeId
          AND lieu_depart = :lieuDepart
          AND lieu_arrive = :lieuArrive
        ORDER BY date_depart_prev ASC
        LIMIT :limit
    """)
    Flux<VoyageEntity> findSimilairesByTrajet(UUID excludeId, String lieuDepart, String lieuArrive, LocalDateTime now, int limit);

    @Query("""
        SELECT v.* FROM voyages v
        JOIN lignes_voyage l ON l.id_voyage = v.id_voyage
        WHERE v.status_voyage = 'PUBLIE'
          AND v.date_depart_prev > :now
          AND v.id_voyage != :excludeId
          AND l.id_agence_voyage = :agenceId
        ORDER BY v.date_depart_prev ASC
        LIMIT :limit
    """)
    Flux<VoyageEntity> findSimilairesByAgence(UUID excludeId, UUID agenceId, LocalDateTime now, int limit);

    @Query("""
        SELECT DISTINCT v.* FROM voyages v
        LEFT JOIN lignes_voyage l ON l.id_voyage = v.id_voyage
        WHERE v.status_voyage = 'PUBLIE'
          AND (CAST(:lieuDepart AS VARCHAR) IS NULL OR v.lieu_depart ILIKE '%' || :lieuDepart || '%')
          AND (CAST(:lieuArrive AS VARCHAR) IS NULL OR v.lieu_arrive ILIKE '%' || :lieuArrive || '%')
          AND (CAST(:date AS VARCHAR) IS NULL OR DATE(v.date_depart_prev) = CAST(:date AS DATE))
          AND (CAST(:classId AS UUID) IS NULL OR l.id_class_voyage = :classId)
          AND (CAST(:agenceId AS UUID) IS NULL OR l.id_agence_voyage = :agenceId)
        ORDER BY v.date_depart_prev ASC
        LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
    """)
    Flux<VoyageEntity> searchVoyages(String lieuDepart, String lieuArrive, String date, UUID classId, UUID agenceId, Pageable pageable);

    @Query("""
        SELECT COUNT(DISTINCT v.id_voyage) FROM voyages v
        LEFT JOIN lignes_voyage l ON l.id_voyage = v.id_voyage
        WHERE v.status_voyage = 'PUBLIE'
          AND (CAST(:lieuDepart AS VARCHAR) IS NULL OR v.lieu_depart ILIKE '%' || :lieuDepart || '%')
          AND (CAST(:lieuArrive AS VARCHAR) IS NULL OR v.lieu_arrive ILIKE '%' || :lieuArrive || '%')
          AND (CAST(:date AS VARCHAR) IS NULL OR DATE(v.date_depart_prev) = CAST(:date AS DATE))
          AND (CAST(:classId AS UUID) IS NULL OR l.id_class_voyage = :classId)
          AND (CAST(:agenceId AS UUID) IS NULL OR l.id_agence_voyage = :agenceId)
    """)
    Mono<Long> countSearchVoyages(String lieuDepart, String lieuArrive, String date, UUID classId, UUID agenceId);


    // --- LOT 5 : Disponibilité ressources ---
    @Query("""
        SELECT v.id_voyage FROM voyages v
        JOIN lignes_voyage l ON l.id_voyage = v.id_voyage
        WHERE l.id_vehicule = :vehiculeId
          AND v.status_voyage = 'PUBLIE'
          AND v.date_depart_prev <= :end
          AND v.heure_arrive >= :start
    """)
    Flux<UUID> findVoyagesUsingVehiculeBetween(UUID vehiculeId, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT v.id_voyage FROM voyages v
        JOIN lignes_voyage l ON l.id_voyage = v.id_voyage
        JOIN chauffeurs c ON c.id = l.id_chauffeur
        WHERE c.user_id = :chauffeurId
          AND v.status_voyage = 'PUBLIE'
          AND v.date_depart_prev <= :end
          AND v.heure_arrive >= :start
    """)
    Flux<UUID> findVoyagesUsingChauffeurBetween(UUID chauffeurId, LocalDateTime start, LocalDateTime end);

    // --- LOT 8 : Voyages par gare ---
    @Query("""
        SELECT v.* FROM voyages v
        JOIN lignes_voyage l ON l.id_voyage = v.id_voyage
        JOIN agences_voyage a ON a.agency_id = l.id_agence_voyage
        WHERE a.gare_routiere_id = :gareId
          AND (CAST(:dateStr AS VARCHAR) IS NULL OR DATE(v.date_depart_prev) = CAST(:dateStr AS DATE))
        ORDER BY v.date_depart_prev ASC
        LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
    """)
    Flux<VoyageEntity> findByGareIdAndDateOptional(UUID gareId, String dateStr, Pageable pageable);

    @Query("""
        SELECT COUNT(*) FROM voyages v
        JOIN lignes_voyage l ON l.id_voyage = v.id_voyage
        JOIN agences_voyage a ON a.agency_id = l.id_agence_voyage
        WHERE a.gare_routiere_id = :gareId
          AND (CAST(:dateStr AS VARCHAR) IS NULL OR CAST(v.date_depart_prev AS DATE) = CAST(:dateStr AS DATE))
    """)
    Mono<Long> countByGareIdAndDateOptional(UUID gareId, String dateStr);

    // --- LOT 8 : Statistiques BSM ---
    @Query("""
        SELECT COUNT(*) FROM voyages v
        JOIN lignes_voyage l ON l.id_voyage = v.id_voyage
        JOIN agences_voyage a ON a.agency_id = l.id_agence_voyage
        WHERE a.gare_routiere_id = :gareId
          AND v.status_voyage = 'PUBLIE'
          AND v.date_depart_prev > :now
    """)
    Mono<Long> countPublicByGareIdAfter(UUID gareId, LocalDateTime now);

    @Query("""
        SELECT COALESCE(
            AVG(CASE WHEN v.nbr_place_reservable > 0
                     THEN v.nbr_place_confirm::float / v.nbr_place_reservable
                     ELSE 0 END),
            0
        ) FROM voyages v
        JOIN lignes_voyage l ON l.id_voyage = v.id_voyage
        JOIN agences_voyage a ON a.agency_id = l.id_agence_voyage
        WHERE a.gare_routiere_id = :gareId
          AND v.status_voyage = 'PUBLIE'
    """)
    Mono<Double> avgTauxRemplissageByGareId(UUID gareId);



}

