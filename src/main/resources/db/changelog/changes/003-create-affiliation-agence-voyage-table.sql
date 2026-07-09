-- liquibase formatted sql

-- changeset yowyob:3
CREATE TABLE IF NOT EXISTS affiliation_agence_voyage (
    id UUID PRIMARY KEY,
    gare_routiere_id UUID NOT NULL,
    agency_id UUID NOT NULL,
    agency_name VARCHAR(255) NOT NULL,
    statut VARCHAR(50) NOT NULL,
    echeance DATE NOT NULL,
    montant_affiliation NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_gare_routiere_id ON affiliation_agence_voyage (gare_routiere_id);
CREATE INDEX idx_agency_id ON affiliation_agence_voyage (agency_id);
