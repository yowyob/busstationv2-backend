--liquibase formatted sql

--changeset claude:006-1
ALTER TABLE agences_voyage
    ADD COLUMN IF NOT EXISTS moyens_paiement JSONB,
    ADD COLUMN IF NOT EXISTS vehicule_id_defaut UUID,
    ADD COLUMN IF NOT EXISTS chauffeur_id_defaut UUID;

--changeset claude:006-2
COMMENT ON COLUMN agences_voyage.moyens_paiement IS 'Liste JSON des moyens de paiement acceptés (CASH, MOBILE_MONEY, CARD, etc.)';
COMMENT ON COLUMN agences_voyage.vehicule_id_defaut IS 'Véhicule utilisé par défaut lors de la création de voyages';
COMMENT ON COLUMN agences_voyage.chauffeur_id_defaut IS 'Chauffeur utilisé par défaut lors de la création de voyages';