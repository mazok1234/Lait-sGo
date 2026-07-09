BEGIN;

-- ============================================================
-- 1. REFERENCES SYSTEME
-- ============================================================

INSERT INTO ref_statut_vie (libelle)
SELECT * FROM (VALUES ('Veau'), ('Genisse'), ('Vache_active'), ('Reformee'), ('Vendue'), ('Morte')) AS v(libelle)
WHERE NOT EXISTS (SELECT 1 FROM ref_statut_vie s WHERE s.libelle = v.libelle);

INSERT INTO ref_statut_repro (libelle)
SELECT * FROM (VALUES ('Vide'), ('En_chaleur'), ('Inseminee'), ('Gestante')) AS v(libelle)
WHERE NOT EXISTS (SELECT 1 FROM ref_statut_repro s WHERE s.libelle = v.libelle);

INSERT INTO ref_statut_lactation_vache (libelle)
SELECT * FROM (VALUES ('Tarie'), ('En_lactation')) AS v(libelle)
WHERE NOT EXISTS (SELECT 1 FROM ref_statut_lactation_vache s WHERE s.libelle = v.libelle);

INSERT INTO ref_statut_sante (libelle)
SELECT * FROM (VALUES ('Saine'), ('Malade'), ('En_traitement')) AS v(libelle)
WHERE NOT EXISTS (SELECT 1 FROM ref_statut_sante s WHERE s.libelle = v.libelle);

INSERT INTO ref_role_utilisateur (code, libelle) VALUES
    ('admin', 'Administrateur'),
    ('employe', 'Employe')
ON CONFLICT (code) DO NOTHING;

INSERT INTO ref_niveau_alerte (code, libelle, ordre) VALUES
    ('urgent',    'Urgent',    1),
    ('attention', 'Attention', 2),
    ('info',      'Info',      3)
ON CONFLICT (code) DO NOTHING;

-- Catalogue des types d'alerte : ne provient d'aucun fichier .sql existant
-- (table nouvelle, requise par le passage type_alerte texte -> id_type FK).
-- Codes repris tels quels de dataTest.sql / AlerteService.MODULE_PAR_TYPE ;
-- seul le libelle (colonne NOT NULL) est un texte ecrit pour l'occasion.
INSERT INTO ref_type_alerte (code, libelle) VALUES
    ('vaccin_en_retard',   'Vaccin en retard'),
    ('rappel_vaccin',      'Rappel vaccin'),
    ('vaccin_prioritaire', 'Vaccin prioritaire'),
    ('traitement_en_cours','Traitement en cours'),
    ('rappel_velage',      'Rappel velage'),
    ('stock_lait_bas',     'Stock de lait insuffisant'),
    ('stock_aliment_bas',  'Stock aliment insuffisant'),
    ('bcs_hors_plage',     'Score BCS hors plage')
ON CONFLICT (code) DO NOTHING;

INSERT INTO ref_statut_lactation (code, libelle) VALUES
    ('active', 'Active'),
    ('terminee', 'Terminee')
ON CONFLICT (code) DO NOTHING;

INSERT INTO ref_phase_lactation (libelle, jour_min, jour_max)
SELECT * FROM (VALUES
    ('Lactation haute', 0, 60),
    ('Lactation moyenne', 61, 180),
    ('Lactation basse', 181, 300),
    ('Tarie', 301, 365)
) AS v(libelle, jour_min, jour_max)
WHERE NOT EXISTS (SELECT 1 FROM ref_phase_lactation p WHERE p.libelle = v.libelle);

INSERT INTO ref_type_aliment (code, libelle) VALUES
    ('foin', 'Foin'),
    ('ensilage', 'Ensilage'),
    ('concentre', 'Concentre'),
    ('mineral', 'Mineral')
ON CONFLICT (code) DO NOTHING;

INSERT INTO ref_race (code, libelle) VALUES
    ('prim_holstein', 'Prim''Holstein'),
    ('normande',      'Normande'),
    ('montbeliarde',  'Montbeliarde'),
    ('charolaise',    'Charolaise'),
    ('TEST_HOL',      'Test Holstein')
ON CONFLICT (code) DO NOTHING;

INSERT INTO ref_type_ia (libelle)
SELECT * FROM (VALUES
    ('IA fraiche'),
    ('IA congelee'),
    ('IA sexee'),
    ('IA a heure fixe'),
    ('IA apres detection de chaleur'),
    ('IA avec synchronisation (Ovsynch)')
) AS v(libelle)
WHERE NOT EXISTS (SELECT 1 FROM ref_type_ia t WHERE t.libelle = v.libelle);

INSERT INTO protocole_vaccin (nom_vaccin, age_min_jours, age_max_jours, duree_rappel_jours)
SELECT * FROM (VALUES
    ('Vaccin Fievre Aphteuse (Primo)', 60, 120, 180),
    ('Vaccin Charbon Symptomatique', 90, 180, 365),
    ('Rhinotracheite Infectieuse Bovine (IBR)', 150, 360, 365)
) AS v(nom_vaccin, age_min_jours, age_max_jours, duree_rappel_jours)
WHERE NOT EXISTS (SELECT 1 FROM protocole_vaccin p WHERE p.nom_vaccin = v.nom_vaccin);

-- ============================================================
-- 2. UTILISATEURS
-- ============================================================

-- admin@laitgo.mg / Admin123!
INSERT INTO utilisateur (nom, email, id_role, mot_de_passe_hash, actif)
SELECT 'Admin Test', 'admin@laitgo.mg', r.id, '$2y$10$k3CgylH3wYI63CdKC.CpNO0i3RD0ml4ZRYcz7dKt.Q9P7qcJM9uVa', true
FROM ref_role_utilisateur r WHERE r.code = 'admin'
ON CONFLICT (email) DO NOTHING;

-- employe@laitgo.mg / Employe123!
INSERT INTO utilisateur (nom, email, id_role, mot_de_passe_hash, actif)
SELECT 'Employe Test', 'employe@laitgo.mg', r.id, '$2y$10$T3bqAFpDJheUhPSV1w/T9.psStGYQsJUhWglT585Kp3fvd5mqwnzC', true
FROM ref_role_utilisateur r WHERE r.code = 'employe'
ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- 3. ALIMENTS + RATIONS STANDARD (par phase de lactation)
-- ============================================================

-- DOUBLON RESOLU : "Foin"/"Ensilage" sont redefinis avec des valeurs
-- differentes dans db.sql (ufl 0.500/50kg/0.15) et test.sql
-- (ufl 0.650/120kg/320.00). Les deux ne peuvent pas coexister (nom
-- UNIQUE). Version de db.sql conservee (fichier de reference du
-- schema) ; celle de test.sql est ignoree (contrainte NOT EXISTS).
INSERT INTO aliment (nom, id_type_aliment, ufl, pdi_g, seuil_alerte_kg, prix_par_kilo)
SELECT 'Foin', (SELECT id FROM ref_type_aliment WHERE code = 'foin'), 0.500, 45.00, 50, 0.15
WHERE NOT EXISTS (SELECT 1 FROM aliment WHERE nom = 'Foin');

INSERT INTO aliment (nom, id_type_aliment, ufl, pdi_g, seuil_alerte_kg, prix_par_kilo)
SELECT 'Ensilage', (SELECT id FROM ref_type_aliment WHERE code = 'ensilage'), 0.800, 60.00, 100, 0.08
WHERE NOT EXISTS (SELECT 1 FROM aliment WHERE nom = 'Ensilage');

INSERT INTO aliment (nom, id_type_aliment, ufl, pdi_g, seuil_alerte_kg, prix_par_kilo)
SELECT 'Concentre', (SELECT id FROM ref_type_aliment WHERE code = 'concentre'), 1.050, 110.00, 30, 0.35
WHERE NOT EXISTS (SELECT 1 FROM aliment WHERE nom = 'Concentre');

INSERT INTO ration (nom, id_phase_lactation)
SELECT 'Ration lactation haute', p.id FROM ref_phase_lactation p WHERE p.libelle = 'Lactation haute'
    AND NOT EXISTS (SELECT 1 FROM ration r WHERE r.id_phase_lactation = p.id);
INSERT INTO ration (nom, id_phase_lactation)
SELECT 'Ration lactation moyenne', p.id FROM ref_phase_lactation p WHERE p.libelle = 'Lactation moyenne'
    AND NOT EXISTS (SELECT 1 FROM ration r WHERE r.id_phase_lactation = p.id);
INSERT INTO ration (nom, id_phase_lactation)
SELECT 'Ration lactation basse', p.id FROM ref_phase_lactation p WHERE p.libelle = 'Lactation basse'
    AND NOT EXISTS (SELECT 1 FROM ration r WHERE r.id_phase_lactation = p.id);
INSERT INTO ration (nom, id_phase_lactation)
SELECT 'Ration tarie', p.id FROM ref_phase_lactation p WHERE p.libelle = 'Tarie'
    AND NOT EXISTS (SELECT 1 FROM ration r WHERE r.id_phase_lactation = p.id);

INSERT INTO ration_aliment (ration_id, aliment_id, quantite_kg)
SELECT r.id, a.id, x.quantite_kg
FROM (VALUES
    ('Ration lactation haute',   'Foin', 5.0),
    ('Ration lactation haute',   'Ensilage', 3.0),
    ('Ration lactation haute',   'Concentre', 2.0),
    ('Ration lactation moyenne', 'Foin', 4.0),
    ('Ration lactation moyenne', 'Ensilage', 3.0),
    ('Ration lactation moyenne', 'Concentre', 1.5),
    ('Ration lactation basse',   'Foin', 3.0),
    ('Ration lactation basse',   'Ensilage', 2.0),
    ('Ration lactation basse',   'Concentre', 1.0),
    ('Ration tarie',             'Foin', 4.0),
    ('Ration tarie',             'Ensilage', 2.0)
) AS x(ration_nom, aliment_nom, quantite_kg)
JOIN ration r ON r.nom = x.ration_nom
JOIN aliment a ON a.nom = x.aliment_nom
WHERE NOT EXISTS (
    SELECT 1 FROM ration_aliment ra WHERE ra.ration_id = r.id AND ra.aliment_id = a.id
);

-- ============================================================
-- 4. RATIONS SPECIALISEES (production / BCS / sante)
-- ============================================================

INSERT INTO ration (nom, id_phase_lactation, production_min_l, production_max_l, priorite)
SELECT 'Ration haute production', NULL, 25.0, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM ration WHERE nom = 'Ration haute production');

INSERT INTO ration (nom, id_phase_lactation, production_min_l, production_max_l, priorite)
SELECT 'Ration basse production', NULL, NULL, 15.0, 1
WHERE NOT EXISTS (SELECT 1 FROM ration WHERE nom = 'Ration basse production');

INSERT INTO ration (nom, id_phase_lactation, bcs_min, bcs_max, priorite)
SELECT 'Ration BCS faible (maigre)', NULL, NULL, 2.49, 2
WHERE NOT EXISTS (SELECT 1 FROM ration WHERE nom = 'Ration BCS faible (maigre)');

INSERT INTO ration (nom, id_phase_lactation, bcs_min, bcs_max, priorite)
SELECT 'Ration BCS eleve (obese)', NULL, 4.01, NULL, 2
WHERE NOT EXISTS (SELECT 1 FROM ration WHERE nom = 'Ration BCS eleve (obese)');

INSERT INTO ration (nom, id_phase_lactation, id_statut_sante, priorite)
SELECT 'Ration convalescence (malade)', NULL, s.id, 3
FROM ref_statut_sante s WHERE s.libelle = 'Malade'
    AND NOT EXISTS (SELECT 1 FROM ration WHERE nom = 'Ration convalescence (malade)');

INSERT INTO ration (nom, id_phase_lactation, id_statut_sante, priorite)
SELECT 'Ration convalescence (traitement)', NULL, s.id, 3
FROM ref_statut_sante s WHERE s.libelle = 'En_traitement'
    AND NOT EXISTS (SELECT 1 FROM ration WHERE nom = 'Ration convalescence (traitement)');

-- ============================================================
-- 5. MEDICAMENTS / MALADIES
-- ============================================================

INSERT INTO medicament (id, nom, delai_attente_lait_defaut, delai_attente_viande_defaut, prix_unitaire) VALUES
    (1, 'Amoxicilline 15%', 3, 8, 1000),
    (2, 'Oxytetracycline LA', 4, 15, 900.80),
    (3, 'Ivermectine', 0, 21, 1500.00),
    (4, 'Anti-inflammatoire Meloxicam', 2, 5, 7000.25),
    (5, 'Vitamine B12', 0, 0, 4000.00)
ON CONFLICT (id) DO NOTHING;
SELECT setval('medicament_id_seq', GREATEST((SELECT MAX(id) FROM medicament), 1));

INSERT INTO maladie (id, nom, description) VALUES
    (1, 'Mammite', 'Inflammation de la mamelle, souvent d''origine bacterienne'),
    (2, 'Fievre aphteuse', 'Maladie virale tres contagieuse'),
    (3, 'Parasitisme intestinal', 'Infestation par des vers gastro-intestinaux'),
    (4, 'Boiterie', 'Probleme locomoteur, souvent lie aux onglons'),
    (5, 'Fievre de lait (hypocalcemie)', 'Trouble metabolique post-velage')
ON CONFLICT (id) DO NOTHING;
SELECT setval('maladie_id_seq', GREATEST((SELECT MAX(id) FROM maladie), 1));

INSERT INTO maladie_medicament (maladie_id, medicament_id) VALUES
    (1, 1), (1, 2),
    (2, 4),
    (3, 3),
    (4, 4),
    (5, 5)
ON CONFLICT DO NOTHING;

-- ============================================================
-- 6. VACHES DE TEST
-- ============================================================

-- Jeu "alertes" (dataTest.sql)
INSERT INTO vache (numero_boucle, id_race, date_naissance, poids_kg, score_bcs)
VALUES
    ('V001_TEST', (SELECT id FROM ref_race WHERE code = 'prim_holstein'), '2021-01-15', 550.0, 3.50),
    ('V002_TEST', (SELECT id FROM ref_race WHERE code = 'normande'),      '2020-06-10', 600.0, 2.80),
    ('V003_TEST', (SELECT id FROM ref_race WHERE code = 'montbeliarde'),  '2022-03-20', 480.0, 4.80),
    ('V004_TEST', (SELECT id FROM ref_race WHERE code = 'prim_holstein'), '2019-11-05', 620.0, 3.10)
ON CONFLICT (numero_boucle) DO NOTHING;

-- Jeu "graphes admin" (test.sql)
INSERT INTO vache (numero_boucle, id_race, date_naissance, poids_kg, score_bcs)
SELECT 'TEST-001', r.id, DATE '2022-01-10', 545.0, 3.25
FROM ref_race r WHERE r.code = 'TEST_HOL'
ON CONFLICT (numero_boucle) DO NOTHING;

-- Jeu "finance/sante" (Ajout_base_finance_sante.sql)
-- Corrige : vache n'a pas de colonnes "nom"/"race" (texte) - la race
-- est une FK id_race vers ref_race. Le fichier source ne fournissait ni
-- poids_kg ni score_bcs pour ces vaches : laisses a NULL (colonnes
-- nullables), aucune valeur inventee. Le "nom" (Marguerite, etc.)
-- n'a pas d'equivalent dans le schema actuel et n'est donc pas repris.
INSERT INTO vache (numero_boucle, id_race, date_naissance)
SELECT * FROM (VALUES
    ('FR001234567', (SELECT id FROM ref_race WHERE code = 'prim_holstein'), DATE '2021-04-12'),
    ('FR001234568', (SELECT id FROM ref_race WHERE code = 'montbeliarde'),  DATE '2020-09-03'),
    ('FR001234569', (SELECT id FROM ref_race WHERE code = 'normande'),      DATE '2022-01-20'),
    ('FR001234570', (SELECT id FROM ref_race WHERE code = 'prim_holstein'), DATE '2019-11-15'),
    ('FR001234571', (SELECT id FROM ref_race WHERE code = 'montbeliarde'),  DATE '2021-06-30')
) AS v(numero_boucle, id_race, date_naissance)
ON CONFLICT (numero_boucle) DO NOTHING;

-- Statuts vie / repro / lactation / sante - jeu "alertes"
INSERT INTO vache_historique_vie (vache_id, statut_id, date_debut)
SELECT v.id, s.id, '2023-01-01'
FROM vache v, ref_statut_vie s
WHERE v.numero_boucle IN ('V001_TEST','V002_TEST','V003_TEST','V004_TEST')
  AND s.libelle = 'Vache_active'
  AND NOT EXISTS (SELECT 1 FROM vache_historique_vie h WHERE h.vache_id = v.id);

INSERT INTO vache_historique_repro (vache_id, statut_id, date_debut)
SELECT v.id, s.id, '2023-01-01'
FROM vache v, ref_statut_repro s
WHERE v.numero_boucle IN ('V001_TEST','V002_TEST','V003_TEST','V004_TEST')
  AND s.libelle = 'Vide'
  AND NOT EXISTS (SELECT 1 FROM vache_historique_repro h WHERE h.vache_id = v.id);

INSERT INTO vache_historique_lactation (vache_id, statut_id, date_debut)
SELECT v.id, s.id, '2023-01-01'
FROM vache v, ref_statut_lactation_vache s
WHERE v.numero_boucle IN ('V001_TEST','V002_TEST','V003_TEST','V004_TEST')
  AND s.libelle = 'En_lactation'
  AND NOT EXISTS (SELECT 1 FROM vache_historique_lactation h WHERE h.vache_id = v.id);

INSERT INTO vache_historique_sante (vache_id, statut_id, date_debut)
SELECT v.id, s.id, '2023-01-01'
FROM vache v, ref_statut_sante s
WHERE v.numero_boucle IN ('V001_TEST','V002_TEST','V003_TEST','V004_TEST')
  AND s.libelle = 'Saine'
  AND NOT EXISTS (SELECT 1 FROM vache_historique_sante h WHERE h.vache_id = v.id);

-- Historique sante detaille - jeu "finance/sante" (statuts Saine/En_traitement alternes)
INSERT INTO vache_historique_sante (vache_id, statut_id, date_debut, date_fin)
SELECT v.id, s.id, x.date_debut, x.date_fin
FROM (VALUES
    ('FR001234567', 'Saine',         DATE '2026-01-01', DATE '2026-03-17'),
    ('FR001234567', 'En_traitement', DATE '2026-03-18', DATE '2026-03-25'),
    ('FR001234567', 'Saine',         DATE '2026-03-26', NULL),
    ('FR001234568', 'Saine',         DATE '2026-01-01', NULL),
    ('FR001234569', 'Saine',         DATE '2026-01-01', DATE '2026-05-01'),
    ('FR001234569', 'En_traitement', DATE '2026-05-02', NULL),
    ('FR001234570', 'Saine',         DATE '2026-01-01', NULL),
    ('FR001234571', 'Saine',         DATE '2026-01-01', DATE '2026-06-10'),
    ('FR001234571', 'En_traitement', DATE '2026-06-11', NULL)
) AS x(numero_boucle, statut_libelle, date_debut, date_fin)
JOIN vache v ON v.numero_boucle = x.numero_boucle
JOIN ref_statut_sante s ON s.libelle = x.statut_libelle
WHERE NOT EXISTS (
    SELECT 1 FROM vache_historique_sante h
    WHERE h.vache_id = v.id AND h.date_debut = x.date_debut
);

-- ============================================================
-- 7. EVENEMENTS SANTE + TRAITEMENTS
-- (nbr_medicament corrige : la colonne existe sur traitement_sante,
-- pas sur evenement_sante)
-- ============================================================

INSERT INTO evenement_sante (id, vache_id, maladie_id, date_evenement, description)
SELECT 1, v.id, 1, DATE '2026-03-15', 'Mammite clinique detectee sur quartier arriere gauche'
FROM vache v WHERE v.numero_boucle = 'FR001234567'
ON CONFLICT (id) DO NOTHING;

INSERT INTO evenement_sante (id, vache_id, maladie_id, date_evenement, description)
SELECT 2, v.id, 3, DATE '2026-05-02', 'Vers detectes lors du controle de routine'
FROM vache v WHERE v.numero_boucle = 'FR001234569'
ON CONFLICT (id) DO NOTHING;

INSERT INTO evenement_sante (id, vache_id, maladie_id, date_evenement, description)
SELECT 3, v.id, 1, DATE '2026-06-11', 'Mammite subclinique confirmee par CMT'
FROM vache v WHERE v.numero_boucle = 'FR001234571'
ON CONFLICT (id) DO NOTHING;

SELECT setval('evenement_sante_id_seq', GREATEST((SELECT MAX(id) FROM evenement_sante), 1));

INSERT INTO traitement_sante (evenement_sante_id, medicament_id, dose, unite, duree_traitement, delai_attente_j, date_debut, date_fin, nbr_medicament)
SELECT x.evenement_sante_id, x.medicament_id, x.dose, x.unite, x.duree_traitement, x.delai_attente_j, x.date_debut, x.date_fin, x.nbr_medicament
FROM (VALUES
    (1, 1, 10.00, 'ml', 5, 3, DATE '2026-03-18', DATE '2026-03-22', 1),
    (2, 3, 5.00,  'ml', 1, 21, DATE '2026-05-02', DATE '2026-05-02', 1),
    (3, 1, 10.00, 'ml', 5, 3, DATE '2026-06-11', DATE '2026-06-15', 1),
    (3, 4, 20.00, 'ml', 3, 5, DATE '2026-06-11', DATE '2026-06-13', 1)
) AS x(evenement_sante_id, medicament_id, dose, unite, duree_traitement, delai_attente_j, date_debut, date_fin, nbr_medicament)
WHERE NOT EXISTS (
    SELECT 1 FROM traitement_sante t
    WHERE t.evenement_sante_id = x.evenement_sante_id
      AND t.medicament_id = x.medicament_id
      AND t.date_debut = x.date_debut
);

-- ============================================================
-- 8. VACCINATION
-- ============================================================

INSERT INTO historique_vaccin (vache_id, id_protocole_vaccin, date_vaccination, type_injection)
SELECT v.id, p.id_protocole_vaccin, CURRENT_DATE - INTERVAL '200 days', 'IA fraiche'
FROM vache v, protocole_vaccin p
WHERE v.numero_boucle = 'V001_TEST'
  AND p.nom_vaccin = 'Vaccin Fievre Aphteuse (Primo)'
  AND NOT EXISTS (
      SELECT 1 FROM historique_vaccin h WHERE h.vache_id = v.id AND h.id_protocole_vaccin = p.id_protocole_vaccin
  );

INSERT INTO historique_vaccin (vache_id, id_protocole_vaccin, date_vaccination, type_injection)
SELECT v.id, p.id_protocole_vaccin, CURRENT_DATE - INTERVAL '360 days', 'IA congelee'
FROM vache v, protocole_vaccin p
WHERE v.numero_boucle = 'V002_TEST'
  AND p.nom_vaccin = 'Rhinotracheite Infectieuse Bovine (IBR)'
  AND NOT EXISTS (
      SELECT 1 FROM historique_vaccin h WHERE h.vache_id = v.id AND h.id_protocole_vaccin = p.id_protocole_vaccin
  );

INSERT INTO historique_vaccin (vache_id, id_protocole_vaccin, date_vaccination, type_injection)
SELECT v.id, p.id_protocole_vaccin, CURRENT_DATE - INTERVAL '300 days', 'IA fraiche'
FROM vache v, protocole_vaccin p
WHERE v.numero_boucle = 'V003_TEST'
  AND p.nom_vaccin = 'Vaccin Charbon Symptomatique'
  AND NOT EXISTS (
      SELECT 1 FROM historique_vaccin h WHERE h.vache_id = v.id AND h.id_protocole_vaccin = p.id_protocole_vaccin
  );

-- ============================================================
-- 9. REPRODUCTION
-- ============================================================

-- V004 : gestante depuis 250 jours  velage prevu dans ~30 jours
UPDATE vache_historique_repro
SET date_fin = CURRENT_DATE - INTERVAL '1 day'
WHERE vache_id = (SELECT id FROM vache WHERE numero_boucle = 'V004_TEST')
  AND date_fin IS NULL;

INSERT INTO vache_historique_repro (vache_id, statut_id, date_debut)
SELECT v.id, s.id, CURRENT_DATE - INTERVAL '1 day'
FROM vache v, ref_statut_repro s
WHERE v.numero_boucle = 'V004_TEST' AND s.libelle = 'Gestante'
  AND NOT EXISTS (
      SELECT 1 FROM vache_historique_repro h WHERE h.vache_id = v.id AND h.date_fin IS NULL AND h.statut_id = s.id
  );

INSERT INTO reproduction (vache_id, date_ia, gestation_confirmee, date_confirmation_gest, statut_ia)
SELECT v.id, CURRENT_DATE - INTERVAL '250 days', true, CURRENT_DATE - INTERVAL '200 days', 'gestante'
FROM vache v
WHERE v.numero_boucle = 'V004_TEST'
  AND NOT EXISTS (SELECT 1 FROM reproduction r WHERE r.vache_id = v.id);

-- V001 : gestante depuis 220 jours  velage prevu dans ~60 jours
UPDATE vache_historique_repro
SET date_fin = CURRENT_DATE - INTERVAL '1 day'
WHERE vache_id = (SELECT id FROM vache WHERE numero_boucle = 'V001_TEST')
  AND date_fin IS NULL;

INSERT INTO vache_historique_repro (vache_id, statut_id, date_debut)
SELECT v.id, s.id, CURRENT_DATE - INTERVAL '1 day'
FROM vache v, ref_statut_repro s
WHERE v.numero_boucle = 'V001_TEST' AND s.libelle = 'Gestante'
  AND NOT EXISTS (
      SELECT 1 FROM vache_historique_repro h WHERE h.vache_id = v.id AND h.date_fin IS NULL AND h.statut_id = s.id
  );

INSERT INTO reproduction (vache_id, date_ia, gestation_confirmee, date_confirmation_gest, statut_ia)
SELECT v.id, CURRENT_DATE - INTERVAL '220 days', true, CURRENT_DATE - INTERVAL '170 days', 'gestante'
FROM vache v
WHERE v.numero_boucle = 'V001_TEST'
  AND NOT EXISTS (SELECT 1 FROM reproduction r WHERE r.vache_id = v.id);

-- ============================================================
-- 10. LACTATION + PRODUCTION
-- ============================================================

-- V002 : lactation active courte (jeu "alertes")
INSERT INTO lactation (vache_id, numero_lactation, date_debut, id_statut)
SELECT v.id, 1, CURRENT_DATE - INTERVAL '10 days', s.id
FROM vache v, ref_statut_lactation s
WHERE v.numero_boucle = 'V002_TEST' AND s.code = 'active'
  AND NOT EXISTS (SELECT 1 FROM lactation l WHERE l.vache_id = v.id AND l.date_fin IS NULL);

INSERT INTO production (lactation_id, vache_id, date_production, quantite_litres,
                        quantite_matin, quantite_soir, quantite_restante)
SELECT l.id, l.vache_id, CURRENT_DATE, 30.00, 15.00, 15.00, 5.00
FROM lactation l
JOIN vache v ON v.id = l.vache_id
WHERE v.numero_boucle = 'V002_TEST' AND l.date_fin IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM production p WHERE p.lactation_id = l.id AND p.date_production = CURRENT_DATE
  );

-- TEST-001 : lactation active + 6 mois de production (jeu "graphes admin")
INSERT INTO lactation (vache_id, numero_lactation, date_debut, id_statut)
SELECT v.id, 1, DATE '2026-01-01', s.id
FROM vache v
JOIN ref_statut_lactation s ON s.code = 'active'
WHERE v.numero_boucle = 'TEST-001'
  AND NOT EXISTS (SELECT 1 FROM lactation l WHERE l.vache_id = v.id AND l.date_fin IS NULL);

INSERT INTO production (lactation_id, vache_id, date_production, quantite_litres,
                        quantite_matin, quantite_soir, quantite_restante, created_by)
SELECT l.id, l.vache_id, p.date_production, p.quantite_litres, p.quantite_matin, p.quantite_soir,
       p.quantite_litres, (SELECT id FROM utilisateur ORDER BY id LIMIT 1)
FROM (VALUES
    (DATE '2026-01-10', 380.00, 190.00, 190.00),
    (DATE '2026-02-10', 410.00, 205.00, 205.00),
    (DATE '2026-03-10', 435.00, 217.50, 217.50),
    (DATE '2026-04-10', 420.00, 210.00, 210.00),
    (DATE '2026-05-10', 460.00, 230.00, 230.00),
    (DATE '2026-06-10', 490.00, 245.00, 245.00)
) AS p(date_production, quantite_litres, quantite_matin, quantite_soir)
JOIN lactation l ON l.vache_id = (SELECT id FROM vache WHERE numero_boucle = 'TEST-001') AND l.date_fin IS NULL
WHERE NOT EXISTS (
    SELECT 1 FROM production pr WHERE pr.vache_id = l.vache_id AND pr.date_production = p.date_production
);

-- Lactations actives pour les 8 autres vaches, reparties sur les 4 phases
-- (0-60j, 61-180j, 181-300j, 301-365j) afin que la page Rations montre
-- des vaches concernees sur chaque phase, pas seulement V002_TEST/TEST-001.
INSERT INTO lactation (vache_id, numero_lactation, date_debut, id_statut)
SELECT v.id, 1, CURRENT_DATE - (x.jours_en_lait || ' days')::interval, s.id
FROM (VALUES
    ('V001_TEST',   30),   -- Lactation haute
    ('FR001234568', 45),   -- Lactation haute
    ('V003_TEST',   100),  -- Lactation moyenne
    ('FR001234569', 120),  -- Lactation moyenne
    ('V004_TEST',   250),  -- Lactation basse
    ('FR001234570', 220),  -- Lactation basse
    ('FR001234567', 340),  -- Tarie
    ('FR001234571', 350)   -- Tarie
) AS x(numero_boucle, jours_en_lait)
JOIN vache v ON v.numero_boucle = x.numero_boucle
JOIN ref_statut_lactation s ON s.code = 'active'
WHERE NOT EXISTS (SELECT 1 FROM lactation l WHERE l.vache_id = v.id AND l.date_fin IS NULL);

-- Statut vache_historique_lactation correspondant (colonne "LACTATION" du
-- Cheptel), pour rester coherent avec les cycles actifs ci-dessus et
-- ceux de TEST-001/V002_TEST inseres plus haut.
INSERT INTO vache_historique_lactation (vache_id, statut_id, date_debut)
SELECT v.id, s.id, CURRENT_DATE - (x.jours_en_lait || ' days')::interval
FROM (VALUES
    ('TEST-001',     'En_lactation', 189),
    ('FR001234567',  'Tarie',        340),
    ('FR001234568',  'En_lactation', 45),
    ('FR001234569',  'En_lactation', 120),
    ('FR001234570',  'En_lactation', 220),
    ('FR001234571',  'Tarie',        350)
) AS x(numero_boucle, statut_libelle, jours_en_lait)
JOIN vache v ON v.numero_boucle = x.numero_boucle
JOIN ref_statut_lactation_vache s ON s.libelle = x.statut_libelle
WHERE NOT EXISTS (SELECT 1 FROM vache_historique_lactation h WHERE h.vache_id = v.id);

-- ============================================================
-- 11. MOUVEMENTS ALIMENTS
-- ============================================================

-- Stock sous seuil (jeu "alertes") : Foin 500-470=30kg<50kg, Ensilage 500-420=80kg<100kg
INSERT INTO mouvement_aliment (aliment_id, type_mouvement, quantite_kg, date_mouvement)
SELECT a.id, 'entree', 500.00, CURRENT_DATE - INTERVAL '30 days'
FROM aliment a WHERE a.nom = 'Foin'
  AND NOT EXISTS (SELECT 1 FROM mouvement_aliment m WHERE m.aliment_id = a.id AND m.date_mouvement = CURRENT_DATE - INTERVAL '30 days' AND m.type_mouvement = 'entree');

INSERT INTO mouvement_aliment (aliment_id, type_mouvement, quantite_kg, date_mouvement)
SELECT a.id, 'entree', 500.00, CURRENT_DATE - INTERVAL '30 days'
FROM aliment a WHERE a.nom = 'Ensilage'
  AND NOT EXISTS (SELECT 1 FROM mouvement_aliment m WHERE m.aliment_id = a.id AND m.date_mouvement = CURRENT_DATE - INTERVAL '30 days' AND m.type_mouvement = 'entree');

INSERT INTO mouvement_aliment (aliment_id, type_mouvement, quantite_kg, date_mouvement)
SELECT a.id, 'sortie', 470.00, CURRENT_DATE
FROM aliment a WHERE a.nom = 'Foin'
  AND NOT EXISTS (SELECT 1 FROM mouvement_aliment m WHERE m.aliment_id = a.id AND m.date_mouvement = CURRENT_DATE AND m.type_mouvement = 'sortie');

INSERT INTO mouvement_aliment (aliment_id, type_mouvement, quantite_kg, date_mouvement)
SELECT a.id, 'sortie', 420.00, CURRENT_DATE
FROM aliment a WHERE a.nom = 'Ensilage'
  AND NOT EXISTS (SELECT 1 FROM mouvement_aliment m WHERE m.aliment_id = a.id AND m.date_mouvement = CURRENT_DATE AND m.type_mouvement = 'sortie');

-- Historique 6 mois (jeu "graphes admin")
INSERT INTO mouvement_aliment (aliment_id, type_mouvement, quantite_kg, date_mouvement)
SELECT a.id, x.type_mouvement, x.quantite_kg, x.date_mouvement
FROM aliment a
JOIN (VALUES
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
    SELECT 1 FROM mouvement_aliment m
    WHERE m.aliment_id = a.id
      AND m.type_mouvement = x.type_mouvement
      AND m.quantite_kg = x.quantite_kg
      AND m.date_mouvement = x.date_mouvement
);

-- ============================================================
-- 12. VENTES
-- ============================================================

INSERT INTO vente (date_vente, quantite_litres, prix_unitaire, created_by)
SELECT CURRENT_DATE, 25.00, 1500.00, u.id
FROM utilisateur u WHERE u.email = 'admin@laitgo.mg'
  AND NOT EXISTS (
      SELECT 1 FROM vente v WHERE v.date_vente = CURRENT_DATE AND v.quantite_litres = 25.00 AND v.prix_unitaire = 1500.00
  );

INSERT INTO vente (date_vente, quantite_litres, prix_unitaire, created_by)
SELECT v.date_vente, v.quantite_litres, v.prix_unitaire, (SELECT id FROM utilisateur ORDER BY id LIMIT 1)
FROM (VALUES
    (DATE '2026-01-25', 300.00, 1600.00),
    (DATE '2026-02-25', 320.00, 1620.00),
    (DATE '2026-03-25', 350.00, 1650.00),
    (DATE '2026-04-25', 340.00, 1670.00),
    (DATE '2026-05-25', 360.00, 1700.00),
    (DATE '2026-06-25', 390.00, 1720.00)
) AS v(date_vente, quantite_litres, prix_unitaire)
WHERE NOT EXISTS (
    SELECT 1 FROM vente ve
    WHERE ve.date_vente = v.date_vente AND ve.quantite_litres = v.quantite_litres AND ve.prix_unitaire = v.prix_unitaire
);

-- ============================================================
-- 13. ALERTES DE TEST
-- (adapte : id_type via ref_type_alerte au lieu de la colonne
-- type_alerte qui n'existe plus)
-- ============================================================

INSERT INTO alerte (id_niveau, id_type, titre, description, vache_id, acquittee)
SELECT n.id, t.id, x.titre, x.description, vv.id, x.acquittee
FROM (VALUES
    ('urgent', 'vaccin_en_retard', 'Vaccin en retard - V001_TEST',
        'Vaccin Fievre Aphteuse (Primo) en retard de 20 jour(s).', 'V001_TEST', false),
    ('urgent', 'stock_aliment_bas', 'Stock insuffisant - Foin',
        'Stock actuel : 30 kg, seuil configure : 50 kg.', NULL, false),
    ('urgent', 'stock_aliment_bas', 'Stock insuffisant - Ensilage',
        'Stock actuel : 80 kg, seuil configure : 100 kg.', NULL, false),
    ('attention', 'vaccin_prioritaire', 'Vaccin prioritaire dans 5j - V002_TEST',
        'Vaccin Rhinotracheite Infectieuse Bovine (IBR) - rappel proche.', 'V002_TEST', false),
    ('attention', 'stock_lait_bas', 'Stock de lait insuffisant',
        'Stock restant apres vente : 5 L (seuil critique : 50 L).', NULL, false),
    ('attention', 'rappel_velage', 'Rappel velage - V001_TEST',
        'Velage prevu dans 60 jour(s).', 'V001_TEST', false),
    ('info', 'rappel_vaccin', 'Rappel vaccin - V003_TEST',
        'Vaccin Charbon Symptomatique - prochain rappel dans 65 jours.', 'V003_TEST', false),
    ('urgent', 'rappel_velage', 'Rappel velage - V004_TEST',
        'Velage prevu dans 30 jour(s).', 'V004_TEST', false),
    ('attention', 'traitement_en_cours', 'Vache en traitement - V003_TEST [ACQUITTEE]',
        'Test alerte acquittee - affichee en bas de liste.', 'V003_TEST', true)
) AS x(niveau_code, type_code, titre, description, numero_boucle, acquittee)
JOIN ref_niveau_alerte n ON n.code = x.niveau_code
JOIN ref_type_alerte t ON t.code = x.type_code
LEFT JOIN vache vv ON vv.numero_boucle = x.numero_boucle
WHERE NOT EXISTS (SELECT 1 FROM alerte al WHERE al.titre = x.titre);

COMMIT;

-- ============================================================
-- VERIFICATION
-- ============================================================
SELECT n.code AS niveau, t.code AS type, a.titre, a.acquittee, a.created_at::date AS date
FROM alerte a
JOIN ref_niveau_alerte n ON n.id = a.id_niveau
JOIN ref_type_alerte t ON t.id = a.id_type
ORDER BY n.ordre, a.created_at DESC;
