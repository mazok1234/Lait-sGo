-- ============================================================
-- MIGRATION : ajout du produit vendu sur `vente`
--   (lait / engrais / viande / vache / veau)
-- ============================================================
-- ATTENTION : fais un backup avant (ex: pg_dump) !
--   pg_dump -U <user> -d <db> -F c -f backup_avant_migration.dump
--
-- Le script est dans une transaction : si quelque chose ne va pas,
-- tu peux faire ROLLBACK; au lieu de COMMIT; à la fin.
-- ============================================================

BEGIN;

-- --------------------------------------------------------------
-- 1) Table de référence des produits vendus
-- --------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ref_produit (
    id           SERIAL PRIMARY KEY,
    code         VARCHAR(30)  NOT NULL UNIQUE,
    libelle      VARCHAR(100) NOT NULL,
    unite_defaut VARCHAR(10)  NOT NULL
);

INSERT INTO ref_produit (code, libelle, unite_defaut) VALUES
    ('LAIT',    'Lait',    'L'),
    ('ENGRAIS', 'Engrais', 'kg'),
    ('VIANDE',  'Viande',  'kg'),
    ('VACHE',   'Vache',   'unité'),
    ('VEAU',    'Veau',    'unité')
ON CONFLICT (code) DO NOTHING;

-- --------------------------------------------------------------
-- 2) Ajouter la colonne produit_id sur vente (nullable pour l'instant)
-- --------------------------------------------------------------
ALTER TABLE vente ADD COLUMN IF NOT EXISTS produit_id INT REFERENCES ref_produit(id);

-- --------------------------------------------------------------
-- 3) Toutes les ventes existantes étaient du lait -> on les rattache
-- --------------------------------------------------------------
UPDATE vente SET produit_id = (SELECT id FROM ref_produit WHERE code = 'LAIT')
WHERE produit_id IS NULL;

-- --------------------------------------------------------------
-- 4) Rendre la colonne obligatoire
-- --------------------------------------------------------------
ALTER TABLE vente ALTER COLUMN produit_id SET NOT NULL;

-- --------------------------------------------------------------
-- 5) La vue v_offre_vente_jour dépend de la colonne -> la supprimer
--    avant de renommer/retyper, on la recrée à l'étape 7.
-- --------------------------------------------------------------
DROP VIEW IF EXISTS v_offre_vente_jour;

-- --------------------------------------------------------------
-- 6) Renommer quantite_litres -> quantite (générique : L/kg/unité)
--    et élargir la précision du prix (vache/veau peuvent dépasser
--    l'ancien plafond DECIMAL(6,2)).
-- --------------------------------------------------------------
ALTER TABLE vente RENAME COLUMN quantite_litres TO quantite;
ALTER TABLE vente ALTER COLUMN quantite TYPE DECIMAL(10,2);
ALTER TABLE vente ALTER COLUMN prix_unitaire TYPE DECIMAL(10,2);

-- --------------------------------------------------------------
-- 7) Recréer la vue v_offre_vente_jour (ne compte que le lait)
-- --------------------------------------------------------------
CREATE OR REPLACE VIEW v_offre_vente_jour AS
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
    SELECT v.date_vente AS date, SUM(v.quantite) AS vente_l
    FROM vente v
    JOIN ref_produit p ON p.id = v.produit_id
    WHERE p.code = 'LAIT'
    GROUP BY v.date_vente
) vente ON vente.date = jours.date
ORDER BY jours.date;

COMMIT;
-- Si quelque chose ne va pas, remplace COMMIT par: ROLLBACK;
