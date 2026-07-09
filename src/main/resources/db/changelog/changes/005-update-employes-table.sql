--liquibase formatted sql
--changeset yowyob:5

ALTER TABLE employes ADD COLUMN IF NOT EXISTS departement VARCHAR(255);
ALTER TABLE employes ADD COLUMN IF NOT EXISTS statut_employe VARCHAR(50);
ALTER TABLE employes ADD COLUMN IF NOT EXISTS salaire DOUBLE PRECISION;
ALTER TABLE employes ADD COLUMN IF NOT EXISTS date_fin_contrat TIMESTAMP WITH TIME ZONE;
