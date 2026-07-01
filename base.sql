CREATE DATABASE laitgo;
\c laitgo;

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

CREATE TABLE ref_statut_lactation (
    id      SERIAL PRIMARY KEY,
    code    VARCHAR(30)  NOT NULL UNIQUE,   -- active, terminee
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE ref_role_utilisateur (
    id      SERIAL PRIMARY KEY,
    code    VARCHAR(30)  NOT NULL UNIQUE,   -- admin, employe
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE ref_type_evenement_sante (
    id      SERIAL PRIMARY KEY,
    code    VARCHAR(30)  NOT NULL UNIQUE,   -- mammite, boiterie, metrite...
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE ref_niveau_alerte (
    id      SERIAL PRIMARY KEY,
    code    VARCHAR(20)  NOT NULL UNIQUE,   -- danger, warning, info
    libelle VARCHAR(50)  NOT NULL
);

CREATE TABLE ref_type_alerte (
    id      SERIAL PRIMARY KEY,
    code    VARCHAR(40)  NOT NULL UNIQUE,   -- ncs_eleve, chute_production...
    libelle VARCHAR(150) NOT NULL
);

CREATE TABLE ref_type_aliment (
    id      SERIAL PRIMARY KEY,
    code    VARCHAR(30)  NOT NULL UNIQUE,   -- ensilage, foin, concentre, mineral
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE ref_stade_physiologique (
    id       SERIAL PRIMARY KEY,
    code     VARCHAR(30)  NOT NULL UNIQUE,   -- lactation_haute, lactation_milieu...
    libelle  VARCHAR(100) NOT NULL,
    jour_min INT NOT NULL,   -- ex: 0   -- sert à déterminer automatiquement la phase d'une vache
    jour_max INT NOT NULL    -- ex: 100
);

CREATE TABLE utilisateur (
    id                BIGSERIAL    PRIMARY KEY,
    nom               VARCHAR(100) NOT NULL,
    email             VARCHAR(255) NOT NULL UNIQUE,
    id_role           INT          NOT NULL REFERENCES ref_role_utilisateur(id),
    mot_de_passe_hash VARCHAR(255) NOT NULL,
    actif             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE vache (
    id               BIGSERIAL    PRIMARY KEY,
    numero_boucle    VARCHAR(20)  NOT NULL UNIQUE,  
    id_race          INT          NOT NULL REFERENCES ref_race(id),
    date_naissance   DATE         NOT NULL,
    poids_kg         DECIMAL(6,1),
    id_statut        INT          NOT NULL REFERENCES ref_statut_vache(id),
    mere_id          BIGINT       REFERENCES vache(id), 
    score_bcs        DECIMAL(3,2),     
    score_locomotion SMALLINT,           
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE lactation (
    id               BIGSERIAL   PRIMARY KEY,
    vache_id         BIGINT      NOT NULL REFERENCES vache(id),
    numero_lactation SMALLINT    NOT NULL,
    date_debut       DATE        NOT NULL,
    date_fin         DATE,
    id_statut        INT         NOT NULL REFERENCES ref_statut_lactation(id),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE production (
    id              BIGSERIAL    PRIMARY KEY,
    lactation_id    BIGINT       NOT NULL REFERENCES lactation(id),
    vache_id        BIGINT       NOT NULL REFERENCES vache(id),
    date_production DATE         NOT NULL,
    quantite_litres DECIMAL(6,2) NOT NULL,  
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by      BIGINT       REFERENCES utilisateur(id)
);

CREATE TABLE reproduction (
    id                     BIGSERIAL PRIMARY KEY,
    vache_id               BIGINT    NOT NULL REFERENCES vache(id),
    date_ia                DATE      NOT NULL,   
    gestation_confirmee    BOOLEAN,             
    date_confirmation_gest DATE,                 
    date_velage_reel       DATE,               
    sexe_veau              CHAR(1)              
);

CREATE TABLE evenement_sante (
    id                BIGSERIAL   PRIMARY KEY,
    vache_id          BIGINT      NOT NULL REFERENCES vache(id),
    date_evenement    DATE        NOT NULL,
    id_type_evenement INT         NOT NULL REFERENCES ref_type_evenement_sante(id),
    description       TEXT,      
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE aliment (
    id              BIGSERIAL    PRIMARY KEY,
    nom             VARCHAR(100) NOT NULL UNIQUE,
    id_type_aliment INT          NOT NULL REFERENCES ref_type_aliment(id),
    ufl             DECIMAL(5,3),   
    pdi_g           DECIMAL(6,2),  
    seuil_alerte_kg DECIMAL(10,2) DEFAULT 0,  
    prix_par_kilo   DECIMAL(8,2)  
);

CREATE TABLE mouvement_aliment (
    id             BIGSERIAL     PRIMARY KEY,
    aliment_id     BIGINT        NOT NULL REFERENCES aliment(id),
    type_mouvement VARCHAR(10)   NOT NULL,   -- entree / sortie
    quantite_kg    DECIMAL(10,2) NOT NULL,
    date_mouvement DATE          NOT NULL,
    created_by     BIGINT        REFERENCES utilisateur(id),
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE TABLE ration (
    id                     BIGSERIAL    PRIMARY KEY,
    nom                    VARCHAR(100) NOT NULL,
    id_stade_physiologique INT          NOT NULL UNIQUE REFERENCES ref_stade_physiologique(id)
);

CREATE TABLE ration_aliment (
    id          BIGSERIAL    PRIMARY KEY,
    ration_id   BIGINT       NOT NULL REFERENCES ration(id),
    aliment_id  BIGINT       NOT NULL REFERENCES aliment(id),
    quantite_kg DECIMAL(6,2) NOT NULL
);


CREATE TABLE alerte (
    id          BIGSERIAL   PRIMARY KEY,
    id_niveau   INT         NOT NULL REFERENCES ref_niveau_alerte(id),
    id_type     INT         NOT NULL REFERENCES ref_type_alerte(id),
    titre       VARCHAR(200) NOT NULL,
    description TEXT,     
    vache_id    BIGINT      REFERENCES vache(id),
    acquittee   BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE vente (
    id              BIGSERIAL    PRIMARY KEY,
    date_vente      DATE         NOT NULL,
    quantite_litres DECIMAL(8,2) NOT NULL,  
    prix_unitaire   DECIMAL(6,2),           
    created_by      BIGINT       REFERENCES utilisateur(id),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE VIEW v_lactation_total AS
SELECT
    l.id AS lactation_id,
    l.vache_id,
    l.numero_lactation,
    COALESCE(SUM(p.quantite_litres), 0) AS production_totale_l
FROM lactation l
LEFT JOIN production p ON p.lactation_id = l.id
GROUP BY l.id, l.vache_id, l.numero_lactation;

CREATE VIEW v_reproduction_suivi AS
SELECT
    r.id,
    r.vache_id,
    r.date_ia,
    r.date_ia + INTERVAL '280 days' AS date_velage_prevu,
    r.gestation_confirmee,
    r.date_confirmation_gest,
    r.date_velage_reel,
    r.sexe_veau
FROM reproduction r;

CREATE VIEW v_ration_nutrition AS
SELECT
    r.id AS ration_id,
    r.nom,
    COALESCE(SUM(ra.quantite_kg * a.ufl), 0)                     AS ufl_total,
    COALESCE(SUM(ra.quantite_kg * a.pdi_g), 0)                   AS pdi_total_g,
    COALESCE(SUM(ra.quantite_kg * a.prix_par_kilo), 0) AS cout_j_eur
FROM ration r
LEFT JOIN ration_aliment ra ON ra.ration_id = r.id
LEFT JOIN aliment a ON a.id = ra.aliment_id
GROUP BY r.id, r.nom;

CREATE VIEW v_stock_aliment AS
SELECT
    a.id AS aliment_id,
    a.nom,
    a.seuil_alerte_kg,
    COALESCE(SUM(CASE WHEN m.type_mouvement = 'entree' THEN m.quantite_kg ELSE 0 END), 0)
        - COALESCE(SUM(CASE WHEN m.type_mouvement = 'sortie' THEN m.quantite_kg ELSE 0 END), 0)
        AS stock_actuel_kg
FROM aliment a
LEFT JOIN mouvement_aliment m ON m.aliment_id = a.id
GROUP BY a.id, a.nom, a.seuil_alerte_kg;

CREATE VIEW v_ration_recommandee AS
SELECT
    v.id AS vache_id,
    v.numero_boucle,
    (CURRENT_DATE - l.date_debut) AS jours_en_lait,
    sp.libelle AS phase_actuelle,
    r.id AS ration_id,
    r.nom AS ration_recommandee
FROM vache v
JOIN lactation l ON l.vache_id = v.id
    AND l.id_statut = (SELECT id FROM ref_statut_lactation WHERE code = 'active')
JOIN ref_stade_physiologique sp
    ON (CURRENT_DATE - l.date_debut) BETWEEN sp.jour_min AND sp.jour_max
JOIN ration r ON r.id_stade_physiologique = sp.id;

CREATE VIEW v_offre_vente_jour AS
SELECT
    jours.date AS date,
    COALESCE(offre.production_l, 0) AS offre_litres,
    COALESCE(vente.vente_l, 0)      AS vente_litres,
    COALESCE(offre.production_l, 0) - COALESCE(vente.vente_l, 0) AS ecart_litres
FROM (
    SELECT date_production AS date FROM production
    UNION
    SELECT date_vente AS date FROM vente
) jours
LEFT JOIN (
    SELECT date_production AS date, SUM(quantite_litres) AS production_l
    FROM production
    GROUP BY date_production
) offre ON offre.date = jours.date
LEFT JOIN (
    SELECT date_vente AS date, SUM(quantite_litres) AS vente_l
    FROM vente
    GROUP BY date_vente
) vente ON vente.date = jours.date
ORDER BY jours.date;