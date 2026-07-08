-- Donnees de test pour visualiser les graphes admin (production, ventes, depenses)
-- Execution: psql -U postgres -d laitgo -f test.sql

BEGIN;

-- 1) References minimales
INSERT INTO ref_type_aliment (code, libelle)
VALUES
    ('FOURRAGE', 'Fourrage'),
    ('CONCENTRE', 'Concentre')
ON CONFLICT (code) DO NOTHING;

INSERT INTO ref_race (code, libelle)
VALUES ('TEST_HOL', 'Test Holstein')
ON CONFLICT (code) DO NOTHING;

-- 2) Aliments (idempotent)
INSERT INTO aliment (nom, id_type_aliment, ufl, pdi_g, seuil_alerte_kg, prix_par_kilo)
SELECT 'Foin', t.id, 0.650, 85.00, 120.00, 320.00
FROM ref_type_aliment t
WHERE t.code = 'FOURRAGE'
ON CONFLICT (nom) DO UPDATE
SET
    id_type_aliment = EXCLUDED.id_type_aliment,
    ufl = EXCLUDED.ufl,
    pdi_g = EXCLUDED.pdi_g,
    seuil_alerte_kg = EXCLUDED.seuil_alerte_kg,
    prix_par_kilo = EXCLUDED.prix_par_kilo;

INSERT INTO aliment (nom, id_type_aliment, ufl, pdi_g, seuil_alerte_kg, prix_par_kilo)
SELECT 'Ensilage', t.id, 0.450, 65.00, 180.00, 180.00
FROM ref_type_aliment t
WHERE t.code = 'FOURRAGE'
ON CONFLICT (nom) DO UPDATE
SET
    id_type_aliment = EXCLUDED.id_type_aliment,
    ufl = EXCLUDED.ufl,
    pdi_g = EXCLUDED.pdi_g,
    seuil_alerte_kg = EXCLUDED.seuil_alerte_kg,
    prix_par_kilo = EXCLUDED.prix_par_kilo;

-- 3) Mouvements aliments sur 6 mois (entrees et sorties)
INSERT INTO mouvement_aliment (aliment_id, type_mouvement, quantite_kg, date_mouvement, created_at)
SELECT a.id, x.type_mouvement, x.quantite_kg, x.date_mouvement, NOW()
FROM aliment a
JOIN (
    VALUES
        ('Foin', 'entree', 520.00, DATE '2026-01-03'),
        ('Foin', 'sortie', 220.00, DATE '2026-01-20'),
        ('Foin', 'entree', 540.00, DATE '2026-02-03'),
        ('Foin', 'sortie', 225.00, DATE '2026-02-20'),
        ('Foin', 'entree', 560.00, DATE '2026-03-03'),
        ('Foin', 'sortie', 230.00, DATE '2026-03-20'),
        ('Foin', 'entree', 570.00, DATE '2026-04-03'),
        ('Foin', 'sortie', 235.00, DATE '2026-04-20'),
        ('Foin', 'entree', 590.00, DATE '2026-05-03'),
        ('Foin', 'sortie', 240.00, DATE '2026-05-20'),
        ('Foin', 'entree', 610.00, DATE '2026-06-03'),
        ('Foin', 'sortie', 250.00, DATE '2026-06-20'),

        ('Ensilage', 'entree', 820.00, DATE '2026-01-04'),
        ('Ensilage', 'sortie', 260.00, DATE '2026-01-22'),
        ('Ensilage', 'entree', 840.00, DATE '2026-02-04'),
        ('Ensilage', 'sortie', 270.00, DATE '2026-02-22'),
        ('Ensilage', 'entree', 860.00, DATE '2026-03-04'),
        ('Ensilage', 'sortie', 280.00, DATE '2026-03-22'),
        ('Ensilage', 'entree', 880.00, DATE '2026-04-04'),
        ('Ensilage', 'sortie', 290.00, DATE '2026-04-22'),
        ('Ensilage', 'entree', 900.00, DATE '2026-05-04'),
        ('Ensilage', 'sortie', 300.00, DATE '2026-05-22'),
        ('Ensilage', 'entree', 920.00, DATE '2026-06-04'),
        ('Ensilage', 'sortie', 310.00, DATE '2026-06-22')
) AS x(nom_aliment, type_mouvement, quantite_kg, date_mouvement)
    ON a.nom = x.nom_aliment
WHERE NOT EXISTS (
    SELECT 1
    FROM mouvement_aliment m
    WHERE m.aliment_id = a.id
      AND m.type_mouvement = x.type_mouvement
      AND m.quantite_kg = x.quantite_kg
      AND m.date_mouvement = x.date_mouvement
);

-- Ajuste les sorties existantes pour garder des depenses coherentes et un benefice positif
UPDATE mouvement_aliment m
SET quantite_kg = x.quantite_kg
FROM aliment a
JOIN (
    VALUES
        ('Foin', DATE '2026-01-20', 220.00),
        ('Foin', DATE '2026-02-20', 225.00),
        ('Foin', DATE '2026-03-20', 230.00),
        ('Foin', DATE '2026-04-20', 235.00),
        ('Foin', DATE '2026-05-20', 240.00),
        ('Foin', DATE '2026-06-20', 250.00),
        ('Ensilage', DATE '2026-01-22', 260.00),
        ('Ensilage', DATE '2026-02-22', 270.00),
        ('Ensilage', DATE '2026-03-22', 280.00),
        ('Ensilage', DATE '2026-04-22', 290.00),
        ('Ensilage', DATE '2026-05-22', 300.00),
        ('Ensilage', DATE '2026-06-22', 310.00)
) AS x(nom_aliment, date_mouvement, quantite_kg)
    ON a.nom = x.nom_aliment
WHERE m.aliment_id = a.id
  AND LOWER(m.type_mouvement) = 'sortie'
  AND m.date_mouvement = x.date_mouvement;

-- 4) Vache de test + lactation active
INSERT INTO vache (numero_boucle, id_race, date_naissance, poids_kg, score_bcs)
SELECT 'TEST-001', r.id, DATE '2022-01-10', 545.0, 3.25
FROM ref_race r
WHERE r.code = 'TEST_HOL'
ON CONFLICT (numero_boucle) DO NOTHING;

INSERT INTO lactation (vache_id, numero_lactation, date_debut, id_statut)
SELECT v.id, 1, DATE '2026-01-01', s.id
FROM vache v
JOIN ref_statut_lactation s ON s.code = 'active'
WHERE v.numero_boucle = 'TEST-001'
  AND NOT EXISTS (
      SELECT 1
      FROM lactation l
      WHERE l.vache_id = v.id
        AND l.date_fin IS NULL
  );

-- 5) Production mensuelle sur 6 mois
INSERT INTO production (
    lactation_id,
    vache_id,
    date_production,
    quantite_litres,
    quantite_matin,
    quantite_soir,
    quantite_restante,
    created_at,
    created_by
)
SELECT l.id, l.vache_id, p.date_production, p.quantite_litres, p.quantite_matin, p.quantite_soir,
       p.quantite_litres, NOW(),
       (SELECT u.id FROM utilisateur u ORDER BY u.id LIMIT 1)
FROM (
    VALUES
        (DATE '2026-01-10', 380.00, 190.00, 190.00),
        (DATE '2026-02-10', 410.00, 205.00, 205.00),
        (DATE '2026-03-10', 435.00, 217.50, 217.50),
        (DATE '2026-04-10', 420.00, 210.00, 210.00),
        (DATE '2026-05-10', 460.00, 230.00, 230.00),
        (DATE '2026-06-10', 490.00, 245.00, 245.00)
) AS p(date_production, quantite_litres, quantite_matin, quantite_soir)
JOIN lactation l ON l.id = (
    SELECT lx.id
    FROM lactation lx
    WHERE lx.vache_id = (SELECT id FROM vache WHERE numero_boucle = 'TEST-001')
      AND lx.date_fin IS NULL
    ORDER BY lx.id DESC
    LIMIT 1
)
WHERE TRUE
  AND NOT EXISTS (
      SELECT 1
      FROM production pr
      WHERE pr.vache_id = l.vache_id
        AND pr.date_production = p.date_production
  );

-- 6) Ventes mensuelles sur 6 mois
INSERT INTO vente (date_vente, quantite_litres, prix_unitaire, created_by, created_at)
SELECT v.date_vente, v.quantite_litres, v.prix_unitaire,
       (SELECT u.id FROM utilisateur u ORDER BY u.id LIMIT 1),
       NOW()
FROM (
    VALUES
        (DATE '2026-01-25', 300.00, 1600.00),
        (DATE '2026-02-25', 320.00, 1620.00),
        (DATE '2026-03-25', 350.00, 1650.00),
        (DATE '2026-04-25', 340.00, 1670.00),
        (DATE '2026-05-25', 360.00, 1700.00),
        (DATE '2026-06-25', 390.00, 1720.00)
) AS v(date_vente, quantite_litres, prix_unitaire)
WHERE NOT EXISTS (
    SELECT 1
    FROM vente ve
    WHERE ve.date_vente = v.date_vente
      AND ve.quantite_litres = v.quantite_litres
      AND ve.prix_unitaire = v.prix_unitaire
);

-- 7) Rations simples pour affichage module ration (optionnel, sans doublons)
INSERT INTO ration (nom, id_phase_lactation)
SELECT 'Ration Debut Lactation', rp.id
FROM ref_phase_lactation rp
WHERE rp.libelle = 'Lactation haute'
  AND NOT EXISTS (
      SELECT 1 FROM ration r WHERE r.id_phase_lactation = rp.id
  );

INSERT INTO ration (nom, id_phase_lactation)
SELECT 'Ration Milieu Lactation', rp.id
FROM ref_phase_lactation rp
WHERE rp.libelle = 'Lactation moyenne'
  AND NOT EXISTS (
      SELECT 1 FROM ration r WHERE r.id_phase_lactation = rp.id
  );

INSERT INTO ration_aliment (ration_id, aliment_id, quantite_kg)
SELECT r.id, a.id,
       CASE WHEN a.nom = 'Foin' THEN 12.00 ELSE 18.00 END
FROM ration r
JOIN aliment a ON a.nom IN ('Foin', 'Ensilage')
WHERE r.nom = 'Ration Debut Lactation'
  AND NOT EXISTS (
      SELECT 1
      FROM ration_aliment ra
      WHERE ra.ration_id = r.id
        AND ra.aliment_id = a.id
  );

INSERT INTO ration_aliment (ration_id, aliment_id, quantite_kg)
SELECT r.id, a.id,
       CASE WHEN a.nom = 'Foin' THEN 10.00 ELSE 16.00 END
FROM ration r
JOIN aliment a ON a.nom IN ('Foin', 'Ensilage')
WHERE r.nom = 'Ration Milieu Lactation'
  AND NOT EXISTS (
      SELECT 1
      FROM ration_aliment ra
      WHERE ra.ration_id = r.id
        AND ra.aliment_id = a.id
  );

COMMIT;

-- Verification rapide
SELECT EXTRACT(YEAR FROM date_production) AS annee,
       EXTRACT(MONTH FROM date_production) AS mois,
       SUM(quantite_litres) AS total_l
FROM production
GROUP BY EXTRACT(YEAR FROM date_production), EXTRACT(MONTH FROM date_production)
ORDER BY annee, mois;

SELECT EXTRACT(YEAR FROM date_vente) AS annee,
       EXTRACT(MONTH FROM date_vente) AS mois,
       SUM(quantite_litres * prix_unitaire) AS revenus
FROM vente
GROUP BY EXTRACT(YEAR FROM date_vente), EXTRACT(MONTH FROM date_vente)
ORDER BY annee, mois;

SELECT EXTRACT(YEAR FROM m.date_mouvement) AS annee,
       EXTRACT(MONTH FROM m.date_mouvement) AS mois,
       SUM(m.quantite_kg * COALESCE(a.prix_par_kilo, 0)) AS depenses
FROM mouvement_aliment m
JOIN aliment a ON a.id = m.aliment_id
WHERE LOWER(m.type_mouvement) = 'sortie'
GROUP BY EXTRACT(YEAR FROM m.date_mouvement), EXTRACT(MONTH FROM m.date_mouvement)
ORDER BY annee, mois;
