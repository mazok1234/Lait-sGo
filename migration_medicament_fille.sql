-- ============================================================
-- MIGRATION : split de `medicament` -> `medicament` + `medicament_fille`
-- ============================================================
-- ATTENTION : fais un backup avant (ex: pg_dump) !
--   pg_dump -U <user> -d <db> -F c -f backup_avant_migration.dump
--
-- Le script est dans une transaction : si quelque chose ne va pas,
-- tu peux faire ROLLBACK; au lieu de COMMIT; à la fin.
-- ============================================================

BEGIN;

-- --------------------------------------------------------------
-- 1) Nouvelle table medicament_fille
--    (variante d'un médicament : dose / unité / délais d'attente)
--    NB: j'ai gardé delai_attente_viande_defaut et prix_unitaire ici
--    pour ne pas perdre ces données. Supprime-les si tu es sûr de
--    vouloir t'en passer.
-- --------------------------------------------------------------
CREATE TABLE medicament_fille (
    id                          BIGSERIAL     PRIMARY KEY,
    medicament_id               BIGINT        NOT NULL REFERENCES medicament(id) ON DELETE CASCADE,
    dose                        DECIMAL(10,2) NOT NULL,
    unite                       VARCHAR(50)   NOT NULL,
    delai_attente_lait_defaut   INT           DEFAULT 0,
    delai_attente_viande_defaut INT           DEFAULT 0,
    prix_unitaire               DECIMAL(10,2) DEFAULT 0
);

-- --------------------------------------------------------------
-- 2) Peupler medicament_fille à partir des combinaisons existantes
--    (medicament_id, dose, unite) trouvées dans traitement_sante,
--    en récupérant les valeurs par défaut qui étaient sur medicament
-- --------------------------------------------------------------
INSERT INTO medicament_fille (medicament_id, dose, unite, delai_attente_lait_defaut, delai_attente_viande_defaut, prix_unitaire)
SELECT DISTINCT
    t.medicament_id,
    t.dose,
    t.unite,
    m.delai_attente_lait_defaut,
    m.delai_attente_viande_defaut,
    m.prix_unitaire
FROM traitement_sante t
JOIN medicament m ON m.id = t.medicament_id;

-- --------------------------------------------------------------
-- 3) Ajouter la nouvelle colonne FK sur traitement_sante
-- --------------------------------------------------------------
ALTER TABLE traitement_sante ADD COLUMN medicament_fille_id BIGINT;

-- --------------------------------------------------------------
-- 4) Relier chaque traitement à la bonne "fille" (par medicament_id + dose + unite)
-- --------------------------------------------------------------
UPDATE traitement_sante t
SET medicament_fille_id = mf.id
FROM medicament_fille mf
WHERE mf.medicament_id = t.medicament_id
  AND mf.dose = t.dose
  AND mf.unite = t.unite;

-- --------------------------------------------------------------
-- 5) VERIFICATION avant de continuer
--    -> Cette requête doit renvoyer 0 lignes.
--    Si ce n'est pas le cas, NE CONTINUE PAS le script (fais ROLLBACK).
-- --------------------------------------------------------------
DO $$
DECLARE
    orphelins INT;
BEGIN
    SELECT COUNT(*) INTO orphelins
    FROM traitement_sante
    WHERE medicament_fille_id IS NULL;

    IF orphelins > 0 THEN
        RAISE EXCEPTION 'Migration arrêtée : % lignes de traitement_sante sans medicament_fille_id', orphelins;
    END IF;
END $$;

-- --------------------------------------------------------------
-- 6) Rendre la colonne obligatoire + ajouter la contrainte FK
-- --------------------------------------------------------------
ALTER TABLE traitement_sante
    ALTER COLUMN medicament_fille_id SET NOT NULL;

ALTER TABLE traitement_sante
    ADD CONSTRAINT fk_traitement_medicament_fille
    FOREIGN KEY (medicament_fille_id) REFERENCES medicament_fille(id);

-- --------------------------------------------------------------
-- 7) Supprimer les anciennes colonnes de traitement_sante
--    (maintenant portées par medicament_fille, ou abandonnées)
--    ATTENTION: delai_attente_j et nbr_medicament sont perdus ici,
--    car ton nouveau schéma ne les prévoit nulle part.
-- --------------------------------------------------------------
ALTER TABLE traitement_sante
    DROP COLUMN medicament_id,
    DROP COLUMN dose,
    DROP COLUMN unite,
    DROP COLUMN delai_attente_j,
    DROP COLUMN nbr_medicament;

-- --------------------------------------------------------------
-- 8) Nettoyer medicament (il ne reste que id + nom)
-- --------------------------------------------------------------
ALTER TABLE medicament
    DROP COLUMN delai_attente_lait_defaut,
    DROP COLUMN delai_attente_viande_defaut,
    DROP COLUMN prix_unitaire;

-- --------------------------------------------------------------
-- 9) (optionnel mais recommandé) renommer la colonne pour matcher
--    exactement ton schéma cible si tu utilises "medicamentFille_id"
--    côté code au lieu de medicament_fille_id — sinon ignore ceci.
-- --------------------------------------------------------------
-- ALTER TABLE traitement_sante RENAME COLUMN medicament_fille_id TO medicamentFille_id;

COMMIT;
-- Si une vérification échoue plus haut, remplace COMMIT par:
-- ROLLBACK;
ALTER TABLE traitement_sante
    ADD COLUMN IF NOT EXISTS nbr_medicament INT NOT NULL DEFAULT 1;

ALTER TABLE traitement_sante
    ADD COLUMN IF NOT EXISTS delai_attente_j INT NOT NULL DEFAULT 0;


-- ============================================================
-- VIDER ET REPEUPLER medicament (+ medicament_fille + maladie_medicament)
-- ============================================================
-- ATTENTION : medicament_fille -> traitement_sante en CASCADE.
-- Vider medicament supprimera donc aussi TOUS les traitements
-- de santé existants qui référencent ces médicaments.
--
-- Si tu veux juste repartir à zéro sur les médicaments sans
-- toucher à l'historique des traitements, NE LANCE PAS ce script
-- tant que traitement_sante n'est pas vide (vérifie avec la
-- requête de contrôle en bas de fichier).
-- ============================================================

BEGIN;

-- --------------------------------------------------------------
-- 1) Vérification de sécurité : combien de traitements seraient
--    supprimés en cascade si on continue ?
-- --------------------------------------------------------------
DO $$
DECLARE
    nb_traitements INT;
BEGIN
    SELECT COUNT(*) INTO nb_traitements FROM traitement_sante;
    RAISE NOTICE '% traitement(s) existant(s) qui seront supprimés en cascade', nb_traitements;
END $$;

-- --------------------------------------------------------------
-- 2) Vider les tables concernées (ordre important à cause des FK)
--    TRUNCATE ... CASCADE gère l'ordre automatiquement, mais on
--    reste explicite pour la lisibilité.
-- --------------------------------------------------------------
TRUNCATE TABLE traitement_sante RESTART IDENTITY CASCADE;
TRUNCATE TABLE maladie_medicament RESTART IDENTITY CASCADE;
TRUNCATE TABLE medicament_fille RESTART IDENTITY CASCADE;
TRUNCATE TABLE medicament RESTART IDENTITY CASCADE;

-- --------------------------------------------------------------
-- 3) Réinsertion des médicaments (uniquement id + nom maintenant)
--    -> Remplace cette liste par tes vrais médicaments.
-- --------------------------------------------------------------
INSERT INTO medicament (nom) VALUES
    ('Amoxicilline'),
    ('Oxytétracycline'),
    ('Paracétamol vétérinaire'),
    ('Ivermectine');
-- ... ajoute autant de lignes que nécessaire

-- --------------------------------------------------------------
-- 4) Réinsertion des variantes (medicament_fille)
--    Une ligne par combinaison dose/unité/prix pour un médicament.
--    Adapte medicament_id selon l'ordre d'insertion ci-dessus
--    (1 = Amoxicilline, 2 = Oxytétracycline, etc. si RESTART IDENTITY)
-- --------------------------------------------------------------
INSERT INTO medicament_fille (medicament_id, dose, unite, delai_attente_lait_defaut, delai_attente_viande_defaut, prix_unitaire) VALUES
    (1, 10.00, 'ml', 3, 8,  1500.00),
    (1, 20.00, 'ml', 3, 8,  2800.00),
    (2, 15.00, 'ml', 5, 12, 2000.00),
    (3, 5.00,  'ml', 0, 1,  800.00),
    (4, 1.00,  'dose', 0, 15, 3500.00);
-- ... adapte selon tes vraies variantes

-- --------------------------------------------------------------
-- 5) Réinsertion des liens maladie <-> medicament
--    Adapte maladie_id selon ta table `maladie` existante.
-- --------------------------------------------------------------
-- INSERT INTO maladie_medicament (maladie_id, medicament_id) VALUES
--     (1, 1),
--     (1, 2),
--     (2, 3);

COMMIT;
-- Si quelque chose ne va pas, remplace COMMIT par: ROLLBACK;

-- --------------------------------------------------------------
-- Vérification finale
-- --------------------------------------------------------------
-- SELECT * FROM medicament;
-- SELECT * FROM medicament_fille;
INSERT INTO maladie_medicament (maladie_id, medicament_id) VALUES
    (1, 1),
    (2, 4),
    (3, 3),
    (4, 4)
ON CONFLICT DO NOTHING;