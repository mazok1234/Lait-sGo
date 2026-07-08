-- Données de test pour la comparaison de productivité entre races
-- Exécutez ces requêtes dans votre base de données pour tester la fonctionnalité

-- 1. Insertion des races
INSERT INTO ref_race (code, libelle) VALUES 
('prim_holstein', 'Prim''Holstein'),
('normande', 'Normande'),
('montbeliarde', 'Montbéliarde'),
('tarentaise', 'Tarentaise');

-- 2. Vérification des statuts obligatoires (s'ils n'existent pas déjà)
-- Statuts de vie
INSERT IGNORE INTO ref_statut_vie (id, code, libelle) VALUES 
(1, 'genisse', 'Genisse'),
(2, 'veau', 'Veau'),
(3, 'reformee', 'Reformee');

-- Statuts de reproduction
INSERT IGNORE INTO ref_statut_repro (id, code, libelle) VALUES 
(1, 'vide', 'Vide'),
(2, 'gestante', 'Gestante');

-- Statuts de lactation
INSERT IGNORE INTO ref_statut_lactation_vache (id, code, libelle) VALUES 
(1, 'tarie', 'Tarie'),
(2, 'en_lactation', 'En_lactation');

-- Statuts de santé
INSERT IGNORE INTO ref_statut_sante (id, code, libelle) VALUES 
(1, 'saine', 'Saine'),
(2, 'malade', 'Malade');

-- 3. Création des vaches pour chaque race
-- Prim'Holstein (3 vaches)
INSERT INTO vache (numero_boucle, date_naissance, poids_kg, score_bcs, score_locomotion, race_id, mere_id) VALUES
('B001', '2020-01-15', 650.5, 3.2, 1, (SELECT id FROM ref_race WHERE libelle='Prim''Holstein'), NULL),
('B002', '2020-03-22', 645.0, 3.5, 1, (SELECT id FROM ref_race WHERE libelle='Prim''Holstein'), NULL),
('B003', '2020-06-10', 655.0, 3.3, 2, (SELECT id FROM ref_race WHERE libelle='Prim''Holstein'), NULL);

-- Normande (3 vaches)
INSERT INTO vache (numero_boucle, date_naissance, poids_kg, score_bcs, score_locomotion, race_id, mere_id) VALUES
('B004', '2020-02-05', 620.0, 3.4, 1, (SELECT id FROM ref_race WHERE libelle='Normande'), NULL),
('B005', '2020-04-18', 615.5, 3.1, 1, (SELECT id FROM ref_race WHERE libelle='Normande'), NULL),
('B006', '2020-07-25', 625.0, 3.6, 1, (SELECT id FROM ref_race WHERE libelle='Normande'), NULL);

-- Montbéliarde (3 vaches)
INSERT INTO vache (numero_boucle, date_naissance, poids_kg, score_bcs, score_locomotion, race_id, mere_id) VALUES
('B007', '2020-01-30', 640.0, 3.3, 1, (SELECT id FROM ref_race WHERE libelle='Montbéliarde'), NULL),
('B008', '2020-05-12', 635.5, 3.4, 2, (SELECT id FROM ref_race WHERE libelle='Montbéliarde'), NULL),
('B009', '2020-08-05', 642.0, 3.2, 1, (SELECT id FROM ref_race WHERE libelle='Montbéliarde'), NULL);

-- Tarentaise (3 vaches)
INSERT INTO vache (numero_boucle, date_naissance, poids_kg, score_bcs, score_locomotion, race_id, mere_id) VALUES
('B010', '2020-02-28', 590.0, 3.5, 1, (SELECT id FROM ref_race WHERE libelle='Tarentaise'), NULL),
('B011', '2020-06-01', 585.5, 3.3, 1, (SELECT id FROM ref_race WHERE libelle='Tarentaise'), NULL),
('B012', '2020-09-15', 595.0, 3.4, 1, (SELECT id FROM ref_race WHERE libelle='Tarentaise'), NULL);

-- 4. Création des statuts initiaux pour toutes les vaches
INSERT INTO vache_historique_vie (vache_id, statut_id, date_debut, date_fin)
SELECT id, 1, '2020-01-01', NULL FROM vache;

INSERT INTO vache_historique_repro (vache_id, statut_id, date_debut, date_fin)
SELECT id, 1, '2020-01-01', NULL FROM vache;

INSERT INTO vache_historique_lactation (vache_id, statut_id, date_debut, date_fin)
SELECT id, 2, '2023-01-01', NULL FROM vache; -- Mettre en lactation

INSERT INTO vache_historique_sante (vache_id, statut_id, date_debut, date_fin)
SELECT id, 1, '2020-01-01', NULL FROM vache;

-- 5. Création du statut de lactation active pour la production
INSERT IGNORE INTO ref_statut_lactation (id, code, libelle) VALUES (1, 'active', 'Active');

-- 6. Création des lactations pour chaque vache
INSERT INTO lactation (vache_id, numero_lactation, date_debut, statut_id)
SELECT id, 1, '2023-01-01', 1 FROM vache;

-- 7. Ajout des productions laitières (10 jours de production pour chaque vache)
-- Prim'Holstein (moyenne 34L/jour)
INSERT INTO production (vache_id, date_production, quantite_matin, quantite_soir, quantite_litres, quantite_restante, lactation_id)
SELECT v.id, DATE_ADD('2023-01-01', INTERVAL n.jour DAY), 17, 17, 34, 34, l.id
FROM vache v
JOIN lactation l ON v.id = l.vache_id
CROSS JOIN (
    SELECT 0 as jour UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
) n
WHERE v.race_id = (SELECT id FROM ref_race WHERE libelle='Prim''Holstein');

-- Normande (moyenne 28L/jour)
INSERT INTO production (vache_id, date_production, quantite_matin, quantite_soir, quantite_litres, quantite_restante, lactation_id)
SELECT v.id, DATE_ADD('2023-01-01', INTERVAL n.jour DAY), 14, 14, 28, 28, l.id
FROM vache v
JOIN lactation l ON v.id = l.vache_id
CROSS JOIN (
    SELECT 0 as jour UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
) n
WHERE v.race_id = (SELECT id FROM ref_race WHERE libelle='Normande');

-- Montbéliarde (moyenne 30L/jour)
INSERT INTO production (vache_id, date_production, quantite_matin, quantite_soir, quantite_litres, quantite_restante, lactation_id)
SELECT v.id, DATE_ADD('2023-01-01', INTERVAL n.jour DAY), 15, 15, 30, 30, l.id
FROM vache v
JOIN lactation l ON v.id = l.vache_id
CROSS JOIN (
    SELECT 0 as jour UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
) n
WHERE v.race_id = (SELECT id FROM ref_race WHERE libelle='Montbéliarde');

-- Tarentaise (moyenne 25L/jour)
INSERT INTO production (vache_id, date_production, quantite_matin, quantite_soir, quantite_litres, quantite_restante, lactation_id)
SELECT v.id, DATE_ADD('2023-01-01', INTERVAL n.jour DAY), 12.5, 12.5, 25, 25, l.id
FROM vache v
JOIN lactation l ON v.id = l.vache_id
CROSS JOIN (
    SELECT 0 as jour UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
) n
WHERE v.race_id = (SELECT id FROM ref_race WHERE libelle='Tarentaise');

-- Résultats attendus dans la comparaison :
-- Prim'Holstein : 3 vaches × 10 jours × 34L = 1020L | 340L/vache
-- Montbéliarde : 3 vaches × 10 jours × 30L = 900L | 300L/vache
-- Normande : 3 vaches × 10 jours × 28L = 840L | 280L/vache
-- Tarentaise : 3 vaches × 10 jours × 25L = 750L | 250L/vache