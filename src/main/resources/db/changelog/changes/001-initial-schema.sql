--liquibase formatted sql
--changeset yowyob:1

DROP TABLE IF EXISTS agences_voyage CASCADE;
DROP TABLE IF EXISTS gare_routiere CASCADE;

CREATE TABLE IF NOT EXISTS organization (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    updated_by UUID,
    organization_id UUID,
    business_domains UUID[],
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
    keywords TEXT[],
    status VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS coordonnee (
    id UUID PRIMARY KEY,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS users (
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

CREATE TABLE IF NOT EXISTS agences_voyage (
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
    gare_routiere_id UUID
);


CREATE TABLE IF NOT EXISTS gare_routiere (
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

CREATE TABLE IF NOT EXISTS employes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    agence_id UUID,
    manager_id UUID,
    is_active BOOLEAN,
    version INT,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    poste VARCHAR(255),
    date_embauche TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (agence_id) REFERENCES agences_voyage(agency_id),
    FOREIGN KEY (manager_id) REFERENCES employes(id)
);


CREATE TABLE IF NOT EXISTS chauffeurs (
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

CREATE TABLE IF NOT EXISTS vehicules (
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

CREATE TABLE IF NOT EXISTS politiques_annulation (
    id_politique UUID PRIMARY KEY,
    duree_coupon_seconds BIGINT,
    id_agence_voyage UUID,
    FOREIGN KEY (id_agence_voyage) REFERENCES agences_voyage(agency_id)
);

CREATE TABLE IF NOT EXISTS taux_periode (
    id_taux_periode UUID PRIMARY KEY,
    date_debut TIMESTAMP WITH TIME ZONE,
    date_fin TIMESTAMP WITH TIME ZONE,
    taux DOUBLE PRECISION,
    compensation DOUBLE PRECISION,
    id_politique_annulation UUID,
    FOREIGN KEY (id_politique_annulation) REFERENCES politiques_annulation(id_politique)
);


CREATE TABLE IF NOT EXISTS voyages (
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


CREATE TABLE IF NOT EXISTS class_voyage (
    id UUID PRIMARY KEY,
    label VARCHAR(255),
    price DOUBLE PRECISION,
    version INT,
    is_active BOOLEAN
);

CREATE TABLE IF NOT EXISTS lignes_voyage (
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


CREATE TABLE IF NOT EXISTS reservations (
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

CREATE TABLE IF NOT EXISTS passagers (
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

CREATE TABLE IF NOT EXISTS baggages (
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

CREATE TABLE IF NOT EXISTS coupons (
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

CREATE TABLE IF NOT EXISTS historiques (
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

CREATE TABLE IF NOT EXISTS soldes_indemnisation (
    id_solde UUID PRIMARY KEY,
    solde DOUBLE PRECISION,
    type VARCHAR(255),
    id_user UUID,
    id_agence_voyage UUID,
    FOREIGN KEY (id_user) REFERENCES users(user_id),
    FOREIGN KEY (id_agence_voyage) REFERENCES agences_voyage(agency_id)
);

