-- ============================================================
-- base.sql — Schéma complet et final de la base laitgo
--
-- Fusionne le schéma (tables, index, vues) de db.sql, alimentation.sql
-- et Ajout_base_finance_sante.sql en un seul fichier, colonnes finales
-- directement dans les CREATE TABLE (plus besoin des ALTER TABLE
-- historiques). Les fichiers d'origine ne sont pas modifiés/supprimés.
--
-- Utilisation : base.sql + donnee.sql suffisent pour lancer le projet.
--   psql -U postgres -f base.sql
--   psql -U postgres -d laitgo -f donnee.sql
-- ============================================================

CREATE DATABASE laitgo;
\c laitgo;

-- ============================================================
-- 1. TABLES DE RÉFÉRENCE
-- ============================================================

CREATE TABLE ref_statut_vie (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE ref_statut_repro (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE ref_statut_lactation_vache (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE ref_statut_sante (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);

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

CREATE TABLE ref_type_ia (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE ref_statut_lactation (
    id      SERIAL PRIMARY KEY,
    code    VARCHAR(30)  NOT NULL UNIQUE,
    libelle VARCHAR(100) NOT NULL
);

-- ============================================================
-- 2. UTILISATEURS
-- ============================================================

CREATE TABLE utilisateur (
    id                BIGSERIAL    PRIMARY KEY,
    nom               VARCHAR(100) NOT NULL,
    email             VARCHAR(255) NOT NULL UNIQUE,
    id_role           INT          NOT NULL REFERENCES ref_role_utilisateur(id),
    mot_de_passe_hash VARCHAR(255) NOT NULL,
    actif             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 3. CHEPTEL
-- ============================================================

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

-- ============================================================
-- 4. REPRODUCTION
-- ============================================================

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

-- ============================================================
-- 5. LACTATION / PRODUCTION
-- ============================================================

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

-- ============================================================
-- 6. SANTÉ
-- ============================================================

CREATE TABLE maladie (
    id          BIGSERIAL    PRIMARY KEY,
    nom         VARCHAR(150) NOT NULL,
    description TEXT
);


CREATE TABLE medicament (
    id                          BIGSERIAL     PRIMARY KEY,
    nom                         VARCHAR(150)  NOT NULL,
);

CREATE TABLE traitement_fille
(
    id                          BIGSERIAL     PRIMARY KEY,
    id_medicament                         VARCHAR(150)  NOT NULL   REFERENCES medicament(id),
    delai_attente_j     DATE NOT NULL,
    delai_traitement  DATE NOT NULL,
    dose DECIMAL(10,2,)
    unite           VARCHAR(50)   NOT NULL,
    prix    DECIMAL(10,2)
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
    id                 BIGSERIAL     PRIMARY KEY,
    evenement_sante_id BIGINT        NOT NULL REFERENCES evenement_sante(id) ON DELETE CASCADE,
    medicament_id      BIGINT        NOT NULL REFERENCES traitement_fille(id),
    date_debut         DATE          NOT NULL,
    date_fin           DATE          NOT NULL,
    nbr_medicament     INT           NOT NULL DEFAULT 1
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

-- ============================================================
-- 7. ALIMENTATION
-- ============================================================

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

-- id_phase_lactation nullable : une ration standard est liée à une phase,
-- une ration spécialisée (production/BCS/santé, priorite > 0) n'en a pas.
CREATE TABLE ration (
    id                  BIGSERIAL    PRIMARY KEY,
    nom                 VARCHAR(100) NOT NULL,
    id_phase_lactation  INT          UNIQUE REFERENCES ref_phase_lactation(id),
    production_min_l    DECIMAL(5,1),
    production_max_l    DECIMAL(5,1),
    bcs_min             DECIMAL(3,2),
    bcs_max             DECIMAL(3,2),
    id_statut_sante     INT          REFERENCES ref_statut_sante(id),
    priorite            INT          NOT NULL DEFAULT 0
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

CREATE INDEX idx_affectation_vache_actif  ON affectation_ration_vache(vache_id, actif);
CREATE INDEX idx_affectation_ration_actif ON affectation_ration_vache(ration_id, actif);

-- ============================================================
-- 8. ALERTES
-- ============================================================

CREATE TABLE alerte (
    id          BIGSERIAL    PRIMARY KEY,
    id_niveau   INT          NOT NULL REFERENCES ref_niveau_alerte(id),
    id_type     INT          NOT NULL REFERENCES ref_type_alerte(id),
    titre       VARCHAR(200) NOT NULL,
    description TEXT,
    vache_id    BIGINT       REFERENCES vache(id),
    acquittee   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_alerte_acquittee_date ON alerte(acquittee, created_at DESC);

-- ============================================================
-- 9. VENTES
-- ============================================================

CREATE TABLE vente (
    id              BIGSERIAL    PRIMARY KEY,
    date_vente      DATE         NOT NULL,
    quantite_litres DECIMAL(8,2) NOT NULL,
    prix_unitaire   DECIMAL(6,2),
    created_by      BIGINT       REFERENCES utilisateur(id),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 10. VUES
-- ============================================================

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
    COALESCE(SUM(ra.quantite_kg * a.ufl), 0)           AS ufl_total,
    COALESCE(SUM(ra.quantite_kg * a.pdi_g), 0)         AS pdi_total_g,
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

-- Vue de suggestion automatique de ration (production / BCS / santé / phase)
CREATE OR REPLACE VIEW v_suggestion_ration AS
WITH production_moyenne_7j AS (
    SELECT
        p.vache_id,
        AVG(p.quantite_litres) AS moyenne_l
    FROM production p
    WHERE p.date_production >= CURRENT_DATE - 7
    GROUP BY p.vache_id
),
statut_sante_actuel AS (
    SELECT
        vhs.vache_id,
        vhs.statut_id,
        rs.libelle AS statut_libelle
    FROM vache_historique_sante vhs
    JOIN ref_statut_sante rs ON rs.id = vhs.statut_id
    WHERE vhs.date_fin IS NULL
),
bcs_actuel AS (
    SELECT
        v.id AS vache_id,
        v.score_bcs,
        v.score_locomotion
    FROM vache v
    WHERE v.score_bcs IS NOT NULL
)
SELECT
    v.id AS vache_id,
    v.numero_boucle,
    v.score_bcs,
    v.score_locomotion,
    l.date_debut AS debut_lactation,
    (CURRENT_DATE - l.date_debut) AS jours_en_lait,
    sp.libelle AS phase_actuelle,
    sp.id AS phase_id,
    pm.moyenne_l AS production_moyenne_7j,
    ss.statut_id AS statut_sante_id,
    ss.statut_libelle AS statut_sante_libelle,
    -- Ration actuelle (via affectation active)
    ar_actuelle.ration_id AS ration_actuelle_id,
    ra_actuelle_nom.nom AS ration_actuelle_nom,
    -- Ration suggérée
    r_sugg.id AS ration_suggeree_id,
    r_sugg.nom AS ration_suggeree_nom,
    r_sugg.priorite AS priorite_suggestion,
    -- Explication de la suggestion
    CASE
        WHEN ss.statut_id IS NOT NULL AND r_sugg.id_statut_sante IS NOT NULL
            THEN 'Vache ' || ss.statut_libelle || ' - Ration de convalescence recommandée'
        WHEN bcs.score_bcs IS NOT NULL AND (
            (bcs.score_bcs < 2.5 AND r_sugg.bcs_max IS NOT NULL)
            OR (bcs.score_bcs > 4.0 AND r_sugg.bcs_min IS NOT NULL)
        )
            THEN 'BCS ' || bcs.score_bcs || ' anormal - Ration adaptée recommandée'
        WHEN pm.moyenne_l IS NOT NULL AND (
            (pm.moyenne_l > 25.0 AND r_sugg.production_min_l IS NOT NULL)
            OR (pm.moyenne_l < 15.0 AND r_sugg.production_max_l IS NOT NULL)
        )
            THEN 'Production moyenne ' || ROUND(pm.moyenne_l::numeric, 1) || 'L/j - Ration adaptée recommandée'
        ELSE 'Ration standard par phase de lactation'
    END AS raison_suggestion
FROM vache v
JOIN lactation l ON l.vache_id = v.id
    AND l.id_statut = (SELECT id FROM ref_statut_lactation WHERE code = 'active')
JOIN ref_phase_lactation sp
    ON (CURRENT_DATE - l.date_debut) BETWEEN sp.jour_min AND sp.jour_max
LEFT JOIN production_moyenne_7j pm ON pm.vache_id = v.id
LEFT JOIN bcs_actuel bcs ON bcs.vache_id = v.id
LEFT JOIN statut_sante_actuel ss ON ss.vache_id = v.id
-- Ration actuelle via affectation active
LEFT JOIN affectation_ration_vache ar_actuelle
    ON ar_actuelle.vache_id = v.id AND ar_actuelle.actif = TRUE
LEFT JOIN ration ra_actuelle_nom ON ra_actuelle_nom.id = ar_actuelle.ration_id
-- Ration suggérée : on prend la meilleure correspondance par priorité
LEFT JOIN LATERAL (
    SELECT r.id, r.nom, r.priorite, r.id_statut_sante,
           r.production_min_l, r.production_max_l,
           r.bcs_min, r.bcs_max
    FROM ration r
    WHERE (
        -- Ration standard par phase (priorite = 0)
        (r.priorite = 0 AND r.id_phase_lactation = sp.id)
        -- OU ration spécialisée par production
        OR (r.priorite = 1 AND r.id_phase_lactation IS NULL
            AND pm.moyenne_l IS NOT NULL
            AND (r.production_min_l IS NULL OR pm.moyenne_l >= r.production_min_l)
            AND (r.production_max_l IS NULL OR pm.moyenne_l <= r.production_max_l))
        -- OU ration spécialisée par BCS
        OR (r.priorite = 2 AND r.id_phase_lactation IS NULL
            AND bcs.score_bcs IS NOT NULL
            AND (r.bcs_min IS NULL OR bcs.score_bcs >= r.bcs_min)
            AND (r.bcs_max IS NULL OR bcs.score_bcs <= r.bcs_max))
        -- OU ration spécialisée par santé
        OR (r.priorite = 3 AND r.id_phase_lactation IS NULL
            AND ss.statut_id IS NOT NULL
            AND r.id_statut_sante = ss.statut_id)
    )
    ORDER BY r.priorite DESC
    LIMIT 1
) r_sugg ON TRUE
WHERE v.id IS NOT NULL
ORDER BY r_sugg.priorite DESC NULLS LAST, v.numero_boucle;
