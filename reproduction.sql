CREATE DATABASE laitgo1;
\c laitgo1;

-- 1. Tables de référence (Dictionnaires)
CREATE TABLE ref_race (
    id      SERIAL PRIMARY KEY,
    code    VARCHAR(30)  NOT NULL UNIQUE,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE ref_statut_vache (
    id      SERIAL PRIMARY KEY,
    code    VARCHAR(30)  NOT NULL UNIQUE,   -- en_lactation, tarie, gestante, reformee
    libelle VARCHAR(100) NOT NULL
);

-- 2. Table principale Vache
CREATE TABLE vache (
    id               BIGSERIAL    PRIMARY KEY,
    numero_boucle    VARCHAR(20)  NOT NULL UNIQUE,  
    id_race          INT          NOT NULL,
    date_naissance   DATE         NOT NULL,
    poids_kg         DECIMAL(6,1),
    mere_id          BIGINT, 
    score_bcs        DECIMAL(3,2),     
    score_locomotion SMALLINT,           
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_vache_race FOREIGN KEY (id_race) REFERENCES ref_race(id),
    CONSTRAINT fk_vache_mere FOREIGN KEY (mere_id) REFERENCES vache(id)
);

-- 3. Table d'historique des statuts (vache_status)
CREATE TABLE vache_status (
    id        BIGSERIAL PRIMARY KEY,
    id_vache  BIGINT NOT NULL,
    id_status INT NOT NULL,
    id_debut  DATE NOT NULL, -- Date de début du statut actuel
    CONSTRAINT fk_status_vache  FOREIGN KEY (id_vache)  REFERENCES vache(id) ON DELETE CASCADE,
    CONSTRAINT fk_status_ref    FOREIGN KEY (id_status) REFERENCES ref_statut_vache(id)
);