-- test.sql
-- Données de test pour l'application Lait's Go
-- Ce fichier ajoute des vaches, des protocoles de vaccin, des historiques et des alertes.
-- Ne modifie pas les données initiales existantes dans base.sql.

-- Références de base pour les tests
INSERT INTO ref_race (code, libelle)
VALUES
  ('holstein', 'Holstein'),
  ('montbeliarde', 'Montbéliarde')
ON CONFLICT (code) DO NOTHING;

INSERT INTO ref_statut_vache (code, libelle)
VALUES
  ('en_lactation', 'En lactation'),
  ('tarie', 'Tariée'),
  ('gestante', 'Gestante')
ON CONFLICT (code) DO NOTHING;

-- Protocoles de vaccination
INSERT INTO protocole_vaccin (nom_vaccin, age_min_jours, age_max_jours, duree_rappel_jours)
VALUES
  ('IBR', 180, 365, 365),
  ('BVD', 200, 400, 180),
  ('Fièvre Q', 150, 360, 365)
ON CONFLICT (nom_vaccin) DO NOTHING;

-- Vaches de test
INSERT INTO vache (numero_boucle, id_race, date_naissance, poids_kg, id_statut, score_bcs, score_locomotion)
VALUES
  ('FR1234567890', (SELECT id FROM ref_race WHERE code = 'holstein'), '2019-05-14', 650.5, (SELECT id FROM ref_statut_vache WHERE code = 'en_lactation'), 3.40, 1),
  ('FR9876543210', (SELECT id FROM ref_race WHERE code = 'montbeliarde'), '2020-08-22', 620.0, (SELECT id FROM ref_statut_vache WHERE code = 'en_lactation'), 3.10, 2),
  ('FR1122334455', (SELECT id FROM ref_race WHERE code = 'holstein'), '2021-03-10', 540.2, (SELECT id FROM ref_statut_vache WHERE code = 'tarie'), 3.80, 1),
  ('FR6677889900', (SELECT id FROM ref_race WHERE code = 'montbeliarde'), '2018-11-02', 715.7, (SELECT id FROM ref_statut_vache WHERE code = 'gestante'), 3.65, 1)
ON CONFLICT (numero_boucle) DO NOTHING;

-- Historique de vaccination de test
INSERT INTO historique_vaccin (vache_id, id_protocole_vaccin, date_vaccination, type_injection)
SELECT v.id, p.id_protocole_vaccin, '2026-06-20', 'INTRAMUSCULAIRE'
FROM vache v
JOIN protocole_vaccin p ON p.nom_vaccin = 'IBR'
WHERE v.numero_boucle = 'FR1234567890'
  AND NOT EXISTS (
    SELECT 1 FROM historique_vaccin h
    WHERE h.vache_id = v.id
      AND h.id_protocole_vaccin = p.id_protocole_vaccin
      AND h.date_vaccination = '2026-06-20'
  );

INSERT INTO historique_vaccin (vache_id, id_protocole_vaccin, date_vaccination, type_injection)
SELECT v.id, p.id_protocole_vaccin, '2026-05-10', 'INTRAMUSCULAIRE'
FROM vache v
JOIN protocole_vaccin p ON p.nom_vaccin = 'BVD'
WHERE v.numero_boucle = 'FR9876543210'
  AND NOT EXISTS (
    SELECT 1 FROM historique_vaccin h
    WHERE h.vache_id = v.id
      AND h.id_protocole_vaccin = p.id_protocole_vaccin
      AND h.date_vaccination = '2026-05-10'
  );

INSERT INTO historique_vaccin (vache_id, id_protocole_vaccin, date_vaccination, type_injection)
SELECT v.id, p.id_protocole_vaccin, '2026-06-25', 'INTRAMUSCULAIRE'
FROM vache v
JOIN protocole_vaccin p ON p.nom_vaccin = 'Fièvre Q'
WHERE v.numero_boucle = 'FR1122334455'
  AND NOT EXISTS (
    SELECT 1 FROM historique_vaccin h
    WHERE h.vache_id = v.id
      AND h.id_protocole_vaccin = p.id_protocole_vaccin
      AND h.date_vaccination = '2026-06-25'
  );

-- Alertes de test
INSERT INTO alerte (id_niveau, id_type, titre, description, vache_id, acquittee)
SELECT
  (SELECT id FROM ref_niveau_alerte WHERE code = 'urgent'),
  (SELECT id FROM ref_type_alerte WHERE code = 'vaccin_en_retard'),
  'Vaccin en retard — FR1234567890',
  'Vaccin IBR en retard de 3 jours.',
  v.id,
  false
FROM vache v
WHERE v.numero_boucle = 'FR1234567890'
  AND NOT EXISTS (
    SELECT 1 FROM alerte a WHERE a.titre = 'Vaccin en retard — FR1234567890'
  );

INSERT INTO alerte (id_niveau, id_type, titre, description, vache_id, acquittee)
SELECT
  (SELECT id FROM ref_niveau_alerte WHERE code = 'attention'),
  (SELECT id FROM ref_type_alerte WHERE code = 'vaccin_prioritaire'),
  'Vaccin prioritaire — FR9876543210',
  'Vaccin BVD à administrer dans les 48h.',
  v.id,
  false
FROM vache v
WHERE v.numero_boucle = 'FR9876543210'
  AND NOT EXISTS (
    SELECT 1 FROM alerte a WHERE a.titre = 'Vaccin prioritaire — FR9876543210'
  );

INSERT INTO alerte (id_niveau, id_type, titre, description, vache_id, acquittee)
SELECT
  (SELECT id FROM ref_niveau_alerte WHERE code = 'info'),
  (SELECT id FROM ref_type_alerte WHERE code = 'rappel_vaccin'),
  'Rappel vaccin — FR1122334455',
  'Vaccin Fièvre Q à programmer dans 7 jours.',
  v.id,
  false
FROM vache v
WHERE v.numero_boucle = 'FR1122334455'
  AND NOT EXISTS (
    SELECT 1 FROM alerte a WHERE a.titre = 'Rappel vaccin — FR1122334455'
  );

INSERT INTO alerte (id_niveau, id_type, titre, description, vache_id, acquittee)
SELECT
  (SELECT id FROM ref_niveau_alerte WHERE code = 'attention'),
  (SELECT id FROM ref_type_alerte WHERE code = 'stock_lait_bas'),
  'Stock de lait bas — FR6677889900',
  'Stock lait insuffisant après la dernière vente.',
  v.id,
  false
FROM vache v
WHERE v.numero_boucle = 'FR6677889900'
  AND NOT EXISTS (
    SELECT 1 FROM alerte a WHERE a.titre = 'Stock de lait bas — FR6677889900'
  );
