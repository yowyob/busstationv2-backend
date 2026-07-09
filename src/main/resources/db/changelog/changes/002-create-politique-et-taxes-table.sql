-- Create politique_et_taxes table
CREATE TABLE IF NOT EXISTS politique_et_taxes (
    id_politique UUID PRIMARY KEY,
    gare_routiere_id UUID NOT NULL,
    nom_politique VARCHAR(255) NOT NULL,
    description TEXT,
    taux_taxe DOUBLE PRECISION,
    montant_fixe DOUBLE PRECISION,
    date_effet DATE,
    document_url VARCHAR(255),
    type VARCHAR(50) NOT NULL,
    FOREIGN KEY (gare_routiere_id) REFERENCES gare_routiere(id_gare_routiere)
);
