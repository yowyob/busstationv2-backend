-- liquibase formatted sql

-- changeset planning:004-create-planning-tables

-- ============================================
-- Table: plannings_voyage
-- Planning/schedule templates for travel agencies
-- ============================================
CREATE TABLE IF NOT EXISTS plannings_voyage (
                                                id_planning         UUID PRIMARY KEY,
                                                id_agence_voyage    UUID NOT NULL,
                                                nom                 VARCHAR(255) NOT NULL,
    description         TEXT,
    recurrence          VARCHAR(50) NOT NULL,   -- QUOTIDIEN, HEBDOMADAIRE, MENSUEL, ANNUEL
    statut              VARCHAR(50) NOT NULL DEFAULT 'BROUILLON', -- BROUILLON, ACTIF, INACTIF, ARCHIVE
    date_debut          DATE NOT NULL,
    date_fin            DATE,                   -- Nullable for indefinite validity
    date_creation       TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modification   TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_planning_agence FOREIGN KEY (id_agence_voyage)
    REFERENCES agences_voyage(agency_id) ON DELETE CASCADE
    );

-- Index for fast lookup by agency
CREATE INDEX IF NOT EXISTS idx_planning_agence ON plannings_voyage(id_agence_voyage);
-- Index for filtering active plannings
CREATE INDEX IF NOT EXISTS idx_planning_statut ON plannings_voyage(statut);
-- Composite index for public queries (active plannings by agency)
CREATE INDEX IF NOT EXISTS idx_planning_agence_statut ON plannings_voyage(id_agence_voyage, statut);

-- ============================================
-- Table: creneaux_planning
-- Time slots within a planning defining voyage templates
-- ============================================
CREATE TABLE IF NOT EXISTS creneaux_planning (
                                                 id_creneau                  UUID PRIMARY KEY,
                                                 id_planning                 UUID NOT NULL,

    -- Scheduling fields
                                                 jour_semaine                VARCHAR(20),    -- MONDAY, TUESDAY, etc. (for HEBDOMADAIRE)
    jour_mois                   INTEGER,        -- 1-31 (for MENSUEL/ANNUEL)
    mois                        INTEGER,        -- 1-12 (for ANNUEL)

-- Voyage template fields
    titre                       VARCHAR(255),
    description                 TEXT,
    heure_depart                TIME NOT NULL,
    heure_arrivee               TIME,
    duree_estimee_minutes       BIGINT,         -- Duration in minutes
    lieu_depart                 VARCHAR(255) NOT NULL,
    lieu_arrive                 VARCHAR(255) NOT NULL,
    point_de_depart             VARCHAR(255),
    point_arrivee               VARCHAR(255),

    -- Resources
    id_class_voyage             UUID NOT NULL,
    id_vehicule                 UUID,           -- Optional, can be assigned later
    id_chauffeur                UUID,           -- Optional, can be assigned later

-- Reservation settings
    nbr_places_disponibles      INTEGER NOT NULL DEFAULT 0,
    delai_reservation_heures    INTEGER NOT NULL DEFAULT 2,
    delai_confirmation_heures   INTEGER NOT NULL DEFAULT 1,

    -- Media
    small_image                 VARCHAR(500),
    big_image                   VARCHAR(500),
    amenities                   TEXT,           -- CSV string of amenities

    actif                       BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_creneau_planning FOREIGN KEY (id_planning)
    REFERENCES plannings_voyage(id_planning) ON DELETE CASCADE,
    CONSTRAINT fk_creneau_class_voyage FOREIGN KEY (id_class_voyage)
    REFERENCES class_voyage(id),
    CONSTRAINT fk_creneau_vehicule FOREIGN KEY (id_vehicule)
    REFERENCES vehicules(id_vehicule),
    CONSTRAINT fk_creneau_chauffeur FOREIGN KEY (id_chauffeur)
    REFERENCES chauffeurs(id),
    CONSTRAINT chk_jour_mois CHECK (jour_mois IS NULL OR (jour_mois >= 1 AND jour_mois <= 31)),
    CONSTRAINT chk_mois CHECK (mois IS NULL OR (mois >= 1 AND mois <= 12))
    );

-- Index for fast lookup by planning
CREATE INDEX IF NOT EXISTS idx_creneau_planning ON creneaux_planning(id_planning);
-- Index for active creneaux
CREATE INDEX IF NOT EXISTS idx_creneau_actif ON creneaux_planning(id_planning, actif);