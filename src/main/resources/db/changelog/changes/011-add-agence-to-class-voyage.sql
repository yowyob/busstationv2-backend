--liquibase formatted sql
--changeset yowyob:11

ALTER TABLE class_voyage ADD COLUMN id_agence_voyage UUID REFERENCES agences_voyage(agency_id);
