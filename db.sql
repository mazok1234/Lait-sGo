CREATE DATABASE laitgo;
\c laitgo;


INSERT INTO ref_role_utilisateur (code, libelle) VALUES
    ('admin', 'Administrateur'),
    ('employe', 'Employé')
ON CONFLICT (code) DO NOTHING;

-- Compte admin de test : email admin@laitgo.mg / mot de passe Admin123!
INSERT INTO utilisateur (nom, email, id_role, mot_de_passe_hash, actif)
SELECT 'Admin Test', 'admin@laitgo.mg', r.id, '$2y$10$k3CgylH3wYI63CdKC.CpNO0i3RD0ml4ZRYcz7dKt.Q9P7qcJM9uVa', true
FROM ref_role_utilisateur r WHERE r.code = 'admin'
ON CONFLICT (email) DO NOTHING;

-- Compte employé de test : email employe@laitgo.mg / mot de passe Employe123!
INSERT INTO utilisateur (nom, email, id_role, mot_de_passe_hash, actif)
SELECT 'Employe Test', 'employe@laitgo.mg', r.id, '$2y$10$T3bqAFpDJheUhPSV1w/T9.psStGYQsJUhWglT585Kp3fvd5mqwnzC', true
FROM ref_role_utilisateur r WHERE r.code = 'employe'
ON CONFLICT (email) DO NOTHING;



CREATE TABLE ref_statut_vie (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);
INSERT INTO ref_statut_vie (libelle) VALUES
    ('Veau'), ('Genisse'), ('Vache_active'), ('Reformee'), ('Vendue'), ('Morte');

CREATE TABLE ref_statut_repro (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);
INSERT INTO ref_statut_repro (libelle) VALUES
    ('Vide'), ('En_chaleur'), ('Inseminee'), ('Gestante');

CREATE TABLE ref_statut_lactation_vache (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);
INSERT INTO ref_statut_lactation_vache (libelle) VALUES
    ('Tarie'), ('En_lactation');

CREATE TABLE ref_statut_sante (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);
INSERT INTO ref_statut_sante (libelle) VALUES
    ('Saine'), ('Malade'), ('En_traitement');


CREATE TABLE ref_role_utilisateur (
    id      SERIAL PRIMARY KEY,
    code    VARCHAR(30)  NOT NULL UNIQUE,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE ref_niveau_alerte (
    id      SERIAL PRIMARY KEY,
    code    VARCHAR(20)  NOT NULL UNIQUE,
    libelle VARCHAR(50)  NOT NULL,
    ordre   SMALLINT     NOT NULL
);

CREATE TABLE ref_type_alerte (
    id      SERIAL PRIMARY KEY,
    code    VARCHAR(40)  NOT NULL UNIQUE,
    libelle VARCHAR(150) NOT NULL
);

CREATE TABLE ref_type_aliment (
    id      SERIAL PRIMARY KEY,
    code    VARCHAR(30)  NOT NULL UNIQUE,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE ref_race (
    id      SERIAL PRIMARY KEY,
    code    VARCHAR(30)  NOT NULL UNIQUE,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE ref_phase_lactation (
    id          SERIAL PRIMARY KEY,
    libelle     VARCHAR(100) NOT NULL,
    jour_min    INT          NOT NULL,
    jour_max    INT          NOT NULL
);

INSERT INTO ref_phase_lactation (libelle, jour_min, jour_max) VALUES
    ('Lactation haute', 0, 60),
    ('Lactation moyenne', 61, 180),
    ('Lactation basse', 181, 300),
    ('Tarie', 301, 365);

CREATE TABLE ref_type_ia (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
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
    mere_id          BIGINT       REFERENCES vache(id),
    score_bcs        DECIMAL(3,2),
    score_locomotion SMALLINT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE vache_historique_vie (
    id          BIGSERIAL PRIMARY KEY,
    vache_id    BIGINT    NOT NULL REFERENCES vache(id),
    statut_id   INT       NOT NULL REFERENCES ref_statut_vie(id),
    date_debut  DATE      NOT NULL,
    date_fin    DATE
);

CREATE TABLE vache_historique_repro (
    id          BIGSERIAL PRIMARY KEY,
    vache_id    BIGINT    NOT NULL REFERENCES vache(id),
    statut_id   INT       NOT NULL REFERENCES ref_statut_repro(id),
    date_debut  DATE      NOT NULL,
    date_fin    DATE
);

CREATE TABLE vache_historique_lactation (
    id          BIGSERIAL PRIMARY KEY,
    vache_id    BIGINT    NOT NULL REFERENCES vache(id),
    statut_id   INT       NOT NULL REFERENCES ref_statut_lactation_vache(id),
    date_debut  DATE      NOT NULL,
    date_fin    DATE
);

CREATE TABLE vache_historique_sante (
    id          BIGSERIAL PRIMARY KEY,
    vache_id    BIGINT    NOT NULL REFERENCES vache(id),
    statut_id   INT       NOT NULL REFERENCES ref_statut_sante(id),
    date_debut  DATE      NOT NULL,
    date_fin    DATE
);

CREATE INDEX idx_hist_vie_actuel       ON vache_historique_vie       (vache_id, date_fin);
CREATE INDEX idx_hist_repro_actuel     ON vache_historique_repro     (vache_id, date_fin);
CREATE INDEX idx_hist_lactation_actuel ON vache_historique_lactation (vache_id, date_fin);
CREATE INDEX idx_hist_sante_actuel     ON vache_historique_sante     (vache_id, date_fin);


CREATE TABLE reproduction (
    id                     BIGSERIAL PRIMARY KEY,
    vache_id               BIGINT    NOT NULL REFERENCES vache(id),
    date_ia                DATE      NOT NULL,
    gestation_confirmee    BOOLEAN,
    date_confirmation_gest DATE,
    date_velage_reel       DATE,
    sexe_veau              CHAR(1),
    statut_ia              VARCHAR(20) DEFAULT 'en_attente',
    semence                VARCHAR(100),
    inseminateur           VARCHAR(100),
    type_injection         VARCHAR(50)
);

CREATE TABLE ref_statut_lactation (
    id      SERIAL PRIMARY KEY,
    code    VARCHAR(30)  NOT NULL UNIQUE,
    libelle VARCHAR(100) NOT NULL
);
INSERT INTO ref_statut_lactation (code, libelle) VALUES
    ('active', 'Active'),
    ('terminee', 'Terminée');

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
    quantite_matin  DECIMAL(6,2),
    quantite_soir   DECIMAL(6,2),
    quantite_restante DECIMAL(6,2) DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by      BIGINT       REFERENCES utilisateur(id)
);

CREATE TABLE maladie (
    id          BIGSERIAL    PRIMARY KEY,
    nom         VARCHAR(150) NOT NULL,
    description TEXT
);

CREATE TABLE medicament (
    id                         BIGSERIAL    PRIMARY KEY,
    nom                        VARCHAR(150) NOT NULL,
    delai_attente_lait_defaut  INT DEFAULT 0,
    delai_attente_viande_defaut INT DEFAULT 0
);

CREATE TABLE maladie_medicament (
    maladie_id    BIGINT NOT NULL REFERENCES maladie(id) ON DELETE CASCADE,
    medicament_id BIGINT NOT NULL REFERENCES medicament(id) ON DELETE CASCADE,
    PRIMARY KEY (maladie_id, medicament_id)
);

CREATE TABLE evenement_sante (
    id                BIGSERIAL   PRIMARY KEY,
    vache_id          BIGINT      NOT NULL REFERENCES vache(id),
    maladie_id        BIGINT      NOT NULL REFERENCES maladie(id),
    date_evenement    DATE        NOT NULL,
    description       TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE traitement_sante (
    id                 BIGSERIAL  PRIMARY KEY,
    evenement_sante_id BIGINT     NOT NULL REFERENCES evenement_sante(id) ON DELETE CASCADE,
    medicament_id      BIGINT     NOT NULL REFERENCES medicament(id),
    dose               DECIMAL(10,2) NOT NULL,
    unite              VARCHAR(50)   NOT NULL,
    duree_traitement   INT           NOT NULL,
    delai_attente_j    INT           NOT NULL DEFAULT 0,
    date_debut         DATE       NOT NULL,
    date_fin           DATE       NOT NULL
);

CREATE TABLE protocole_vaccin (
    id_protocole_vaccin SERIAL PRIMARY KEY,
    nom_vaccin VARCHAR(255) NOT NULL,
    age_min_jours INT NOT NULL,
    age_max_jours INT NOT NULL,
    duree_rappel_jours INT NOT NULL
);

CREATE TABLE historique_vaccin (
    id_historique_vaccin SERIAL PRIMARY KEY,
    vache_id INT NOT NULL,
    id_protocole_vaccin INT NOT NULL,
    date_vaccination DATE NOT NULL,
    type_injection VARCHAR(50) NOT NULL,
    FOREIGN KEY (vache_id) REFERENCES vache(id),
    FOREIGN KEY (id_protocole_vaccin) REFERENCES protocole_vaccin(id_protocole_vaccin)
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
    type_mouvement VARCHAR(10)   NOT NULL,
    quantite_kg    DECIMAL(10,2) NOT NULL,
    date_mouvement DATE          NOT NULL,
    created_by     BIGINT        REFERENCES utilisateur(id),
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE TABLE ration (
    id                     BIGSERIAL    PRIMARY KEY,
    nom                    VARCHAR(100) NOT NULL,
    id_phase_lactation     INT          NOT NULL UNIQUE REFERENCES ref_phase_lactation(id)
);

CREATE TABLE ration_aliment (
    id          BIGSERIAL PRIMARY KEY,
    ration_id   BIGINT    NOT NULL REFERENCES ration(id),
    aliment_id  BIGINT    NOT NULL REFERENCES aliment(id),
    quantite_kg DECIMAL(6,2) NOT NULL
);

CREATE TABLE affectation_ration_vache (
    id         BIGSERIAL PRIMARY KEY,
    vache_id   BIGINT    NOT NULL REFERENCES vache(id),
    ration_id  BIGINT    NOT NULL REFERENCES ration(id),
    date_debut DATE      NOT NULL,
    date_fin   DATE,
    actif      BOOLEAN   NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_affectation_vache_actif ON affectation_ration_vache(vache_id, actif);
CREATE INDEX idx_affectation_ration_actif ON affectation_ration_vache(ration_id, actif);

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

CREATE INDEX idx_alerte_acquittee_date ON alerte(acquittee, created_at DESC);

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
JOIN ref_phase_lactation sp
    ON (CURRENT_DATE - l.date_debut) BETWEEN sp.jour_min AND sp.jour_max
JOIN ration r ON r.id_phase_lactation = sp.id;

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

CREATE OR REPLACE VIEW v_dashboard_suivi_chaleurs AS
WITH dates_ia AS (
    -- 1. On récupère les inséminations réelles passées ou à venir
    SELECT 
        v.id AS vache_id,
        v.numero_boucle,
        r.date_ia AS date_evenement,
        'Insémination Artificielle'::varchar(50) AS type_evenement,
        r.gestation_confirmee
    FROM reproduction r
    JOIN vache v ON v.id = r.vache_id
    WHERE r.date_velage_reel IS NULL -- On ne suit que la reproduction en cours
),
prochaines_chaleurs_theoriques AS (
    -- 2. On calcule le retour en chaleur théorique (J+21) si la gestation n'est pas encore confirmée
    SELECT 
        v.id AS vache_id,
        v.numero_boucle,
        (r.date_ia + INTERVAL '21 days')::date AS date_evenement,
        'Vigilance Retour Chaleurs (J+21)'::varchar(50) AS type_evenement,
        r.gestation_confirmee
    FROM reproduction r
    JOIN vache v ON v.id = r.vache_id
    WHERE r.date_velage_reel IS NULL 
      AND (r.gestation_confirmee IS NULL OR r.gestation_confirmee = FALSE)
)
-- On fusionne le tout pour alimenter le calendrier du Dashboard
SELECT vache_id, numero_boucle, date_evenement, type_evenement FROM dates_ia
UNION ALL
SELECT vache_id, numero_boucle, date_evenement, type_evenement FROM prochaines_chaleurs_theoriques;


INSERT INTO ref_niveau_alerte (code, libelle, ordre) VALUES
    ('urgent',    'Urgent',    1),
    ('attention', 'Attention', 2),
    ('info',      'Info',      3);

-- Rations par phase de lactation
INSERT INTO ration (nom, id_phase_lactation) VALUES
    ('Ration lactation haute', 1),
    ('Ration lactation moyenne', 2),
    ('Ration lactation basse', 3),
    ('Ration tarie', 4);

INSERT INTO ref_type_aliment (code, libelle) VALUES
    ('foin', 'Foin'),
    ('ensilage', 'Ensilage'),
    ('concentre', 'Concentré'),
    ('mineral', 'Minéral');

-- Ids 1/2/3 attendus par les ration_aliment ci-dessous (Foin/Ensilage/Concentré).
INSERT INTO aliment (nom, id_type_aliment, ufl, pdi_g, seuil_alerte_kg, prix_par_kilo) VALUES
    ('Foin', (SELECT id FROM ref_type_aliment WHERE code = 'foin'), 0.500, 45.00, 50, 0.15),
    ('Ensilage', (SELECT id FROM ref_type_aliment WHERE code = 'ensilage'), 0.800, 60.00, 100, 0.08),
    ('Concentré', (SELECT id FROM ref_type_aliment WHERE code = 'concentre'), 1.050, 110.00, 30, 0.35);

INSERT INTO ration_aliment (ration_id, aliment_id, quantite_kg) VALUES
    (1, 1, 5.0),   -- Foin
    (1, 2, 3.0),   -- Ensilage
    (1, 3, 2.0);   -- Concentré

INSERT INTO ration_aliment (ration_id, aliment_id, quantite_kg) VALUES
    (2, 1, 4.0),
    (2, 2, 3.0),
    (2, 3, 1.5);

INSERT INTO ration_aliment (ration_id, aliment_id, quantite_kg) VALUES
    (3, 1, 3.0),
    (3, 2, 2.0),
    (3, 3, 1.0);

INSERT INTO ration_aliment (ration_id, aliment_id, quantite_kg) VALUES
    (4, 1, 4.0),
    (4, 2, 2.0);

INSERT INTO ref_type_alerte (code, libelle) VALUES
    ('vaccin_en_retard',   'Vaccin en retard'),
    ('rappel_vaccin',      'Rappel vaccin'),
    ('vaccin_prioritaire', 'Vaccin prioritaire'),
    ('stock_lait_bas',     'Stock de lait insuffisant'),
    ('rappel_velage',      'Rappel vêlage'),
    ('rappel_chaleur',     'Rappel vache en chaleur'),
    ('stock_aliment_bas',  'Stock aliment insuffisant'),
    ('bcs_hors_plage',     'Score BCS hors plage');

INSERT INTO ref_type_ia (id, libelle) VALUES
    (1, 'IA fraîche'),
    (2, 'IA congelée'),
    (3,    'IA sexée'),
    (4,  'IA à heure fixe'),
    (5,  'IA après détection de chaleur'),
    (6,    'IA avec synchronisation (Ovsynch)');

INSERT INTO ref_race (id, code, libelle) VALUES 
(1, 'prim_holstein', 'Prim''Holstein'),
(2, 'normande',      'Normande'),
(3, 'montbeliarde',  'Montbéliarde'),
(4, 'charolaise',    'Charolaise');

INSERT INTO protocole_vaccin (nom_vaccin, age_min_jours, age_max_jours, duree_rappel_jours) VALUES 
('Vaccin Fièvre Aphteuse (Primo)', 60, 120, 180),    -- À faire entre 2 et 4 mois, rappel tous les 6 mois
('Vaccin Charbon Symptomatique', 90, 180, 365),     -- À faire à partir de 3 mois, rappel annuel
('Rhinotrachéite Infectieuse Bovine (IBR)', 150, 360, 365);