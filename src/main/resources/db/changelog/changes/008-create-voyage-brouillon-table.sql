--liquibase formatted sql

--changeset claude:008-1
CREATE TABLE IF NOT EXISTS voyages_brouillon (
    id UUID PRIMARY KEY,
    agence_voyage_id UUID NOT NULL,
    ligne_service_id UUID,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    lieu_depart VARCHAR(255) NOT NULL,
    lieu_arrive VARCHAR(255) NOT NULL,
    point_de_depart VARCHAR(255),
    point_arrivee VARCHAR(255),
    date_depart_prev TIMESTAMP WITH TIME ZONE,
    heure_depart_effectif TIMESTAMP WITH TIME ZONE,
    heure_arrive TIMESTAMP WITH TIME ZONE,
    duree_estimee VARCHAR(50),
    class_voyage_id UUID,
    vehicule_id UUID,
    chauffeur_id UUID,
    nbr_place_reservable INT,
    prix DOUBLE PRECISION,
    amenities TEXT,
    small_image VARCHAR(512),
    big_image VARCHAR(512),
    date_limite_reservation TIMESTAMP WITH TIME ZONE,
    date_limite_confirmation TIMESTAMP WITH TIME ZONE,
    statut_brouillon VARCHAR(20) NOT NULL DEFAULT 'INCOMPLET',
    notes TEXT,
    voyage_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

--changeset claude:008-2
CREATE INDEX IF NOT EXISTS idx_voyages_brouillon_agence ON voyages_brouillon(agence_voyage_id);
CREATE INDEX IF NOT EXISTS idx_voyages_brouillon_statut ON voyages_brouillon(statut_brouillon);