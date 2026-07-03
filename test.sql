INSERT INTO ref_statut_vache(code, libelle) VALUES
('en_lactation','En lactation'),
('tarie','Tarie'),
('chaleur','En chaleur'),
('gestation','En gestation'),
('velage','Vêlage'),
('reformee','Réformée');
INSERT INTO vache
(numero_boucle,id_race,date_naissance,poids_kg,id_statut,score_bcs,score_locomotion)
VALUES

('MG001',1,'2021-04-15',650,1,3.50,1),   -- En lactation

('MG002',2,'2020-08-18',620,3,3.20,1),   -- En chaleur

('MG003',1,'2019-06-22',670,4,3.60,1),   -- Gestation

('MG004',3,'2022-02-10',590,2,3.10,1),   -- Tarie

('MG005',2,'2018-11-05',700,5,3.80,2),   -- Vêlage

('MG006',1,'2018-03-12',690,6,2.70,3);   -- Réformée
INSERT INTO vache_statut(vache_id,statut_id,date_debut,date_fin)
VALUES

(1,3,'2024-12-20','2025-01-10'),
(1,4,'2025-01-11','2025-02-14'),
(1,5,'2025-02-15','2025-02-20'),
(1,1,'2025-02-21',NULL),

(2,3,'2025-06-20',NULL),

(3,3,'2025-03-15','2025-04-02'),
(3,4,'2025-04-03',NULL),

(4,2,'2025-05-01',NULL),

(5,5,'2026-07-01',NULL),

(6,6,'2026-06-10',NULL);
INSERT INTO lactation
(vache_id,numero_lactation,date_debut,id_statut)
VALUES

(1,2,'2025-02-21',1),

(4,1,'2024-08-10',2),

(5,3,'2026-07-01',1);
INSERT INTO production
(lactation_id,vache_id,date_production,quantite_litres,quantite_restante,created_by)
VALUES

(1,1,'2026-07-01',28.5,5.3,2),
(1,1,'2026-07-02',29.1,4.7,2),
(1,1,'2026-07-03',30.0,6.0,2),

(3,5,'2026-07-02',18.4,3.0,3),
(3,5,'2026-07-03',19.2,2.8,3);

INSERT INTO reproduction
(vache_id,date_ia,gestation_confirmee,date_confirmation_gest,date_velage_reel,sexe_veau)
VALUES

(1,'2024-05-15',TRUE,'2024-06-10','2025-02-15','F'),

(2,'2026-07-02',FALSE,NULL,NULL,NULL),

(3,'2026-03-15',TRUE,'2026-04-12',NULL,NULL),

(5,'2025-10-01',TRUE,'2025-11-02','2026-07-01','M');

INSERT INTO evenement_sante
(vache_id,date_evenement,id_type_evenement,description)
VALUES

(1,'2026-06-15',1,'Mammite légère'),

(3,'2026-05-10',2,'Boiterie légère'),

(5,'2026-07-01',4,'Surveillance après vêlage');
-- =============================================================================
-- 1. DONNÉES DE RÉFÉRENCE SUPPLÉMENTAIRES (Nécessaires pour la cohérence)
-- =============================================================================

INSERT INTO ref_race (code, libelle) VALUES 
('holstein', 'Frisonne Pie Noire (Holstein)'),
('jersey', 'Jersiaise');

INSERT INTO ref_statut_vache (code, libelle) VALUES 
('en_lactation', 'En lactation'),
('tarie', 'Tarie'),
('gestante', 'Gestante'),
('reformee', 'Réformée');

INSERT INTO ref_statut_lactation (code, libelle) VALUES 
('active', 'Active'),
('terminee', 'Terminée');

INSERT INTO ref_role_utilisateur (code, libelle) VALUES 
('admin', 'Administrateur'),
('employe', 'Employé');

INSERT INTO utilisateur (nom, email, id_role, mot_de_passe_hash, actif) VALUES 
('Jean Rabe', 'jean.rabe@laitgo.mg', (SELECT id FROM ref_role_utilisateur WHERE code = 'admin'), 'hash_password_123', true),
('Marie Elena', 'marie.elena@laitgo.mg', (SELECT id FROM ref_role_utilisateur WHERE code = 'employe'), 'hash_password_456', true);


-- =============================================================================
-- 2. INSERTION DU CHEPTEL (Vaches de différents âges pour les tests)
-- =============================================================================

INSERT INTO vache (numero_boucle, id_race, date_naissance, poids_kg, id_statut, score_bcs, score_locomotion) VALUES 
-- Une jeune génisse (environ 3 mois / 90 jours) pour tester les premiers vaccins
('V001_GENISSE', (SELECT id FROM ref_race WHERE code = 'holstein'), CURRENT_DATE - INTERVAL '90 days', 110.0, (SELECT id FROM ref_statut_vache WHERE code = 'tarie'), 3.00, 1),

-- Une vache adulte en lactation
('V002_ADULTE', (SELECT id FROM ref_race WHERE code = 'holstein'), CURRENT_DATE - INTERVAL '4 years', 620.0, (SELECT id FROM ref_statut_vache WHERE code = 'en_lactation'), 3.25, 1),

-- Une vache en fin de gestation avec un score BCS faible (pour déclencher une alerte)
('V003_ALERTE', (SELECT id FROM ref_race WHERE code = 'jersey'), CURRENT_DATE - INTERVAL '5 years', 480.0, (SELECT id FROM ref_statut_vache WHERE code = 'gestante'), 2.20, 2);


-- =============================================================================
-- 3. MODULE VACCIN (Protocoles et Historique)
-- =============================================================================

-- Création des protocoles vaccinaux (ex: Fièvre Aphteuse, Charbon Symptomatique, etc.)
INSERT INTO protocole_vaccin (nom_vaccin, age_min_jours, age_max_jours, duree_rappel_jours) VALUES 
('Vaccin Fièvre Aphteuse (Primo)', 60, 120, 180),    -- À faire entre 2 et 4 mois, rappel tous les 6 mois
('Vaccin Charbon Symptomatique', 90, 180, 365),     -- À faire à partir de 3 mois, rappel annuel
('Rhinotrachéite Infectieuse Bovine (IBR)', 150, 360, 365);

-- Historique des vaccinations passées
INSERT INTO historique_vaccin (vache_id, id_protocole_vaccin, date_vaccination, type_injection) VALUES 
-- La génisse V001 a reçu sa primo-injection de Fièvre Aphteuse il y a 10 jours
((SELECT id FROM vache WHERE numero_boucle = 'V001_GENISSE'), 
 (SELECT id_protocole_vaccin FROM protocole_vaccin WHERE nom_vaccin = 'Vaccin Fièvre Aphteuse (Primo)'), 
 CURRENT_DATE - INTERVAL '10 days', 'Intramusculaire'),

-- La vache V002 a reçu son vaccin IBR l'année dernière (Rappel bientôt nécessaire)
((SELECT id FROM vache WHERE numero_boucle = 'V002_ADULTE'), 
 (SELECT id_protocole_vaccin FROM protocole_vaccin WHERE nom_vaccin = 'Rhinotrachéite Infectieuse Bovine (IBR)'), 
 CURRENT_DATE - INTERVAL '350 days', 'Sous-cutanée'),

-- La vache V003 a reçu son vaccin contre le Charbon il y a un an et demi (Elle est en retard !)
((SELECT id FROM vache WHERE numero_boucle = 'V003_ALERTE'), 
 (SELECT id_protocole_vaccin FROM protocole_vaccin WHERE nom_vaccin = 'Vaccin Charbon Symptomatique'), 
 CURRENT_DATE - INTERVAL '550 days', 'Intramusculaire');


-- =============================================================================
-- 4. MODULE ALERTE
-- =============================================================================

INSERT INTO alerte (id_niveau, id_type, titre, description, vache_id, acquittee) VALUES 
-- Alerte Vaccin en retard (Lié à la vache V003 et son historique en retard)
((SELECT id FROM ref_niveau_alerte WHERE code = 'urgent'), 
 (SELECT id FROM ref_type_alerte WHERE code = 'vaccin_en_retard'), 
 'Retard critique : Vaccin Charbon', 
 'Le vaccin contre le Charbon Symptomatique a dépassé la date limite de rappel de plus de 6 mois.', 
 (SELECT id FROM vache WHERE numero_boucle = 'V003_ALERTE'), false),

-- Alerte Rappel de vaccin (Lié à la vache V002 qui approche des 365 jours)
((SELECT id FROM ref_niveau_alerte WHERE code = 'attention'), 
 (SELECT id FROM ref_type_alerte WHERE code = 'rappel_vaccin'), 
 'Rappel à planifier : Vaccin IBR', 
 'Le rappel annuel pour le vaccin IBR est à prévoir dans les 15 prochains jours.', 
 (SELECT id FROM vache WHERE numero_boucle = 'V002_ADULTE'), false),

-- Alerte Cheptel (BCS hors plage pour la vache V003 qui a un score de 2.20)
((SELECT id FROM ref_niveau_alerte WHERE code = 'urgent'), 
 (SELECT id FROM ref_type_alerte WHERE code = 'bcs_hors_plage'), 
 'Alerte Nutrition : Score BCS trop faible', 
 'La vache présente un score corporel (BCS) de 2.20, ce qui est en dessous du seuil critique pour une vache gestante.', 
 (SELECT id FROM vache WHERE numero_boucle = 'V003_ALERTE'), false),

-- Une alerte déjà traitée/acquittée pour l'historique
((SELECT id FROM ref_niveau_alerte WHERE code = 'info'), 
 (SELECT id FROM ref_type_alerte WHERE code = 'rappel_chaleur'), 
 'Suivi de chaleur acquitté', 
 'Vache détectée en chaleur en début de semaine, IA planifiée.', 
 (SELECT id FROM vache WHERE numero_boucle = 'V002_ADULTE'), true);
