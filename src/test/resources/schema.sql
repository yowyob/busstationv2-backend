-- src/test/resources/schema.sql

-- Initial cleanup to ensure a clean state
DROP TABLE IF EXISTS alertes_agence CASCADE;
DROP TABLE IF EXISTS voyages_brouillon CASCADE;
DROP TABLE IF EXISTS affiliation_agence_voyage CASCADE;
DROP TABLE IF EXISTS politique_et_taxes CASCADE;
DROP TABLE IF EXISTS plannings_voyage CASCADE;
DROP TABLE IF EXISTS creneaux_planning CASCADE;
DROP TABLE IF EXISTS coupons CASCADE;
DROP TABLE IF EXISTS baggages CASCADE;
DROP TABLE IF EXISTS passagers CASCADE;
DROP TABLE IF EXISTS historiques CASCADE;
DROP TABLE IF EXISTS soldes_indemnisation CASCADE;
DROP TABLE IF EXISTS taux_periode CASCADE;
DROP TABLE IF EXISTS politiques_annulation CASCADE;
DROP TABLE IF EXISTS lignes_voyage CASCADE;
DROP TABLE IF EXISTS reservations CASCADE;
DROP TABLE IF EXISTS voyages CASCADE;
DROP TABLE IF EXISTS vehicules CASCADE;
DROP TABLE IF EXISTS chauffeurs CASCADE;
DROP TABLE IF EXISTS employes CASCADE;
DROP TABLE IF EXISTS class_voyage CASCADE;
DROP TABLE IF EXISTS agences_voyage CASCADE;
DROP TABLE IF EXISTS gare_routiere CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS organization CASCADE;
DROP TABLE IF EXISTS coordonnee CASCADE;

-- Tables from 001-initial-schema.sql (PostgreSQL -> H2 compatible)

CREATE TABLE organization (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    updated_by UUID,
    organization_id UUID,
    business_domains TEXT, -- H2 doesn't support UUID[] easily, using TEXT
    email VARCHAR(255),
    short_name VARCHAR(255),
    long_name VARCHAR(255),
    description TEXT,
    logo_url VARCHAR(255),
    is_individual_business BOOLEAN,
    legal_form VARCHAR(255),
    is_active BOOLEAN,
    website_url VARCHAR(255),
    social_network VARCHAR(255),
    business_registration_number VARCHAR(255),
    tax_number VARCHAR(255),
    capital_share DOUBLE PRECISION,
    registration_date TIMESTAMP WITH TIME ZONE,
    ceo_name VARCHAR(255),
    year_founded TIMESTAMP WITH TIME ZONE,
    keywords TEXT, -- H2 doesn't support TEXT[] easily
    status VARCHAR(255)
);

CREATE TABLE coordonnee (
    id UUID PRIMARY KEY,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION
);

CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    nom VARCHAR(255),
    prenom VARCHAR(255),
    genre VARCHAR(50),
    username VARCHAR(255) UNIQUE,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    tel_number VARCHAR(255) UNIQUE,
    roles VARCHAR(255),
    business_actor_type VARCHAR(255),
    address VARCHAR(255),
    idcoordonnee_gps UUID,
    FOREIGN KEY (idcoordonnee_gps) REFERENCES coordonnee(id)
);

CREATE TABLE agences_voyage (
    agency_id UUID PRIMARY KEY,
    name VARCHAR(255),
    short_name VARCHAR(100),
    logo VARCHAR(255),
    version INT,
    is_public BOOLEAN,
    is_active BOOLEAN,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    organisation_id UUID,
    user_id UUID,
    location VARCHAR(255),
    social_network VARCHAR(255),
    description TEXT,
    greeting_message TEXT,
    gare_routiere_id UUID,
    -- Columns from 005 and extras
    logo_url VARCHAR(500),
    rating DOUBLE PRECISION DEFAULT 0.0,
    specialties TEXT,
    moyens_paiement TEXT,
    vehicule_id_defaut UUID,
    chauffeur_id_defaut UUID
);

CREATE TABLE gare_routiere (
    id_gare_routiere UUID PRIMARY KEY,
    version INT,
    is_active BOOLEAN,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    nom_gare_routiere VARCHAR(255),
    adresse VARCHAR(255),
    ville VARCHAR(255),
    quartier VARCHAR(255),
    description TEXT,
    services VARCHAR(512),
    horaires VARCHAR(255),
    photo_url VARCHAR(255),
    nom_president VARCHAR(255),
    manager_id UUID,
    id_coordonnee_gps UUID,
    FOREIGN KEY (manager_id) REFERENCES users(user_id),
    FOREIGN KEY (id_coordonnee_gps) REFERENCES coordonnee(id)
);

CREATE TABLE employes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    agence_id UUID,
    manager_id UUID,
    is_active BOOLEAN,
    version INT,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    poste VARCHAR(255),
    departement VARCHAR(255),
    statut_employe VARCHAR(50),
    salaire DOUBLE PRECISION,
    date_embauche TIMESTAMP WITH TIME ZONE,
    date_fin_contrat TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (agence_id) REFERENCES agences_voyage(agency_id),
    FOREIGN KEY (manager_id) REFERENCES employes(id)
);

CREATE TABLE chauffeurs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    agence_id UUID,
    statut VARCHAR(50),
    version INT,
    is_active BOOLEAN,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    annee_experience INT,
    numero_permis VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (agence_id) REFERENCES agences_voyage(agency_id)
);

CREATE TABLE vehicules (
    id_vehicule UUID PRIMARY KEY,
    nom VARCHAR(255),
    modele VARCHAR(255),
    description TEXT,
    nbr_places INT,
    plaque_matricule VARCHAR(255) UNIQUE,
    lien_photo VARCHAR(255),
    id_agence_voyage UUID,
    FOREIGN KEY (id_agence_voyage) REFERENCES agences_voyage(agency_id)
);

CREATE TABLE politiques_annulation (
    id_politique UUID PRIMARY KEY,
    duree_coupon_seconds BIGINT,
    id_agence_voyage UUID,
    FOREIGN KEY (id_agence_voyage) REFERENCES agences_voyage(agency_id)
);

CREATE TABLE taux_periode (
    id_taux_periode UUID PRIMARY KEY,
    date_debut TIMESTAMP WITH TIME ZONE,
    date_fin TIMESTAMP WITH TIME ZONE,
    taux DOUBLE PRECISION,
    compensation DOUBLE PRECISION,
    id_politique_annulation UUID,
    FOREIGN KEY (id_politique_annulation) REFERENCES politiques_annulation(id_politique)
);

CREATE TABLE voyages (
    id_voyage UUID PRIMARY KEY,
    titre VARCHAR(255),
    description TEXT,
    date_depart_prev TIMESTAMP WITH TIME ZONE,
    lieu_depart VARCHAR(255),
    date_depart_effectif TIMESTAMP WITH TIME ZONE,
    date_arrive_effectif TIMESTAMP WITH TIME ZONE,
    lieu_arrive VARCHAR(255),
    heure_depart_effectif TIMESTAMP WITH TIME ZONE,
    point_de_depart VARCHAR(255),
    point_arrivee VARCHAR(255),
    duree_voyage BIGINT,
    heure_arrive TIMESTAMP WITH TIME ZONE,
    nbr_place_reservable INT,
    nbr_place_reserve INT,
    nbr_place_confirm INT,
    nbr_place_restante INT,
    date_publication TIMESTAMP WITH TIME ZONE,
    date_limite_reservation TIMESTAMP WITH TIME ZONE,
    date_limite_confirmation TIMESTAMP WITH TIME ZONE,
    status_voyage VARCHAR(50),
    small_image VARCHAR(255),
    big_image VARCHAR(255),
    amenities TEXT
);

CREATE TABLE class_voyage (
    id UUID PRIMARY KEY,
    label VARCHAR(255),
    price DOUBLE PRECISION,
    version INT,
    is_active BOOLEAN,
    id_agence_voyage UUID REFERENCES agences_voyage(agency_id) -- from original schema.sql
);

CREATE TABLE lignes_voyage (
    id_ligne_voyage UUID PRIMARY KEY,
    id_class_voyage UUID,
    id_vehicule UUID,
    id_voyage UUID,
    id_agence_voyage UUID,
    id_chauffeur UUID,
    FOREIGN KEY (id_class_voyage) REFERENCES class_voyage(id),
    FOREIGN KEY (id_vehicule) REFERENCES vehicules(id_vehicule),
    FOREIGN KEY (id_voyage) REFERENCES voyages(id_voyage),
    FOREIGN KEY (id_agence_voyage) REFERENCES agences_voyage(agency_id),
    FOREIGN KEY (id_chauffeur) REFERENCES chauffeurs(id)
);

CREATE TABLE reservations (
    id_reservation UUID PRIMARY KEY,
    date_reservation TIMESTAMP WITH TIME ZONE,
    date_confirmation TIMESTAMP WITH TIME ZONE,
    nbr_passager INT,
    prix_total DOUBLE PRECISION,
    statut_reservation VARCHAR(50),
    id_user UUID,
    id_voyage UUID,
    statut_payement VARCHAR(50),
    transaction_code VARCHAR(255),
    montant_paye DOUBLE PRECISION,
    FOREIGN KEY (id_user) REFERENCES users(user_id),
    FOREIGN KEY (id_voyage) REFERENCES voyages(id_voyage)
);

CREATE TABLE passagers (
    id_passager UUID PRIMARY KEY,
    numero_piece_identific VARCHAR(255),
    nom VARCHAR(255),
    genre VARCHAR(50),
    age INT,
    nbr_baggage INT,
    id_reservation UUID,
    place_choisis INT,
    FOREIGN KEY (id_reservation) REFERENCES reservations(id_reservation)
);

CREATE TABLE baggages (
    id UUID PRIMARY KEY,
    baggage_id UUID NOT NULL,
    version INT,
    is_paid BOOLEAN,
    reservation_id UUID,
    passager_id UUID,
    extra_fees DOUBLE PRECISION,
    weight DOUBLE PRECISION,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id_reservation),
    FOREIGN KEY (passager_id) REFERENCES passagers(id_passager)
);

CREATE TABLE coupons (
    id UUID PRIMARY KEY,
    coupon_id UUID NOT NULL,
    version INT,
    reservation_id UUID,
    user_id UUID,
    code VARCHAR(255) UNIQUE,
    is_used BOOLEAN,
    amount DOUBLE PRECISION,
    expiry_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id_reservation),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE historiques (
    id_historique UUID PRIMARY KEY,
    status_historique VARCHAR(50),
    date_reservation TIMESTAMP WITH TIME ZONE,
    date_confirmation TIMESTAMP WITH TIME ZONE,
    date_annulation TIMESTAMP WITH TIME ZONE,
    cause_annulation TEXT,
    origine_annulation VARCHAR(255),
    taux_annulation DOUBLE PRECISION,
    compensation DOUBLE PRECISION,
    id_reservation UUID,
    FOREIGN KEY (id_reservation) REFERENCES reservations(id_reservation)
);

CREATE TABLE soldes_indemnisation (
    id_solde UUID PRIMARY KEY,
    solde DOUBLE PRECISION,
    type VARCHAR(255),
    id_user UUID,
    id_agence_voyage UUID,
    FOREIGN KEY (id_user) REFERENCES users(user_id),
    FOREIGN KEY (id_agence_voyage) REFERENCES agences_voyage(agency_id)
);

-- Table from 002-create-politique-et-taxes-table.sql
CREATE TABLE politique_et_taxes (
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

-- Table from 003-create-affiliation-agence-voyage-table.sql
CREATE TABLE affiliation_agence_voyage (
    id UUID PRIMARY KEY,
    gare_routiere_id UUID NOT NULL,
    agency_id UUID NOT NULL,
    agency_name VARCHAR(255) NOT NULL,
    statut VARCHAR(50) NOT NULL,
    echeance DATE NOT NULL,
    montant_affiliation NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tables from 004-create-planning-tables.sql
CREATE TABLE plannings_voyage (
    id_planning UUID PRIMARY KEY,
    id_agence_voyage UUID NOT NULL,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    recurrence VARCHAR(50) NOT NULL,
    statut VARCHAR(50) NOT NULL DEFAULT 'BROUILLON',
    date_debut DATE NOT NULL,
    date_fin DATE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_planning_agence FOREIGN KEY (id_agence_voyage) REFERENCES agences_voyage(agency_id) ON DELETE CASCADE
);

CREATE TABLE creneaux_planning (
    id_creneau UUID PRIMARY KEY,
    id_planning UUID NOT NULL,
    jour_semaine VARCHAR(20),
    jour_mois INTEGER,
    mois INTEGER,
    titre VARCHAR(255),
    description TEXT,
    heure_depart TIME NOT NULL,
    heure_arrivee TIME,
    duree_estimee_minutes BIGINT,
    lieu_depart VARCHAR(255) NOT NULL,
    lieu_arrive VARCHAR(255) NOT NULL,
    point_de_depart VARCHAR(255),
    point_arrivee VARCHAR(255),
    id_class_voyage UUID NOT NULL,
    id_vehicule UUID,
    id_chauffeur UUID,
    nbr_places_disponibles INTEGER NOT NULL DEFAULT 0,
    delai_reservation_heures INTEGER NOT NULL DEFAULT 2,
    delai_confirmation_heures INTEGER NOT NULL DEFAULT 1,
    small_image VARCHAR(500),
    big_image VARCHAR(500),
    amenities TEXT,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_creneau_planning FOREIGN KEY (id_planning) REFERENCES plannings_voyage(id_planning) ON DELETE CASCADE,
    CONSTRAINT fk_creneau_class_voyage FOREIGN KEY (id_class_voyage) REFERENCES class_voyage(id),
    CONSTRAINT fk_creneau_vehicule FOREIGN KEY (id_vehicule) REFERENCES vehicules(id_vehicule)
);

-- Table from 008-create-voyage-brouillon-table.sql
CREATE TABLE voyages_brouillon (
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
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_brouillon_agence FOREIGN KEY (agence_voyage_id) REFERENCES agences_voyage(agency_id) ON DELETE CASCADE
);

-- Table from 009-create-alertes-agence-table.sql
CREATE TABLE alertes_agence (
    id_alerte UUID PRIMARY KEY,
    gare_id UUID NOT NULL,
    agence_id UUID NOT NULL,
    bsm_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    is_lu BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lu_at TIMESTAMP,
    FOREIGN KEY (gare_id) REFERENCES gare_routiere(id_gare_routiere),
    FOREIGN KEY (agence_id) REFERENCES agences_voyage(agency_id),
    FOREIGN KEY (bsm_id) REFERENCES users(user_id)
);

