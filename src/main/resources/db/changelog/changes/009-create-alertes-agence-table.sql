-- liquibase formatted sql

-- changeset yowyob:9
CREATE TABLE IF NOT EXISTS alertes_agence (
    id_alerte    UUID PRIMARY KEY,
    gare_id      UUID         NOT NULL,
    agence_id    UUID         NOT NULL,
    bsm_id       UUID         NOT NULL,
    type         VARCHAR(50)  NOT NULL,
    message      TEXT         NOT NULL,
    is_lu        BOOLEAN      NOT NULL DEFAULT false,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lu_at        TIMESTAMP    NULL,
    FOREIGN KEY (gare_id)   REFERENCES gare_routiere(id_gare_routiere),
    FOREIGN KEY (agence_id) REFERENCES agences_voyage(agency_id),
    FOREIGN KEY (bsm_id)    REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_alertes_gare    ON alertes_agence(gare_id);
CREATE INDEX IF NOT EXISTS idx_alertes_agence  ON alertes_agence(agence_id);
CREATE INDEX IF NOT EXISTS idx_alertes_lu      ON alertes_agence(is_lu);