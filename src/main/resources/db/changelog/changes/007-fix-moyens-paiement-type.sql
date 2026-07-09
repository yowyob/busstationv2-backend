--liquibase formatted sql

--changeset claude:007-1
ALTER TABLE agences_voyage
    ALTER COLUMN moyens_paiement TYPE TEXT USING moyens_paiement::TEXT;