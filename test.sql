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