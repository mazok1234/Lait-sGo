-- Scripts d’ajout / migration
-- Ajoute colonne prix_unitaire dans medicament
-- Ajoute colonne nbr_medicament dans evenement_sante

ALTER TABLE medicament
    ADD COLUMN IF NOT EXISTS prix_unitaire DECIMAL(10,2) DEFAULT 0;

ALTER TABLE evenement_sante
    ADD COLUMN IF NOT EXISTS nbr_medicament INT DEFAULT 0;

--Donne test--

INSERT INTO medicament (id, nom, delai_attente_lait_defaut, delai_attente_viande_defaut, prix_unitaire) VALUES
(1, 'Amoxicilline 15%', 3, 8, 1000),
(2, 'Oxytetracycline LA', 4, 15, 900.80),
(3, 'Ivermectine', 0, 21, 1500.00),
(4, 'Anti-inflammatoire Meloxicam', 2, 5, 7000.25),
(5, 'Vitamine B12', 0, 0, 4000.00);

-- ============================================
-- MALADIE
-- ============================================
INSERT INTO maladie (id, nom, description) VALUES
(1, 'Mammite', 'Inflammation de la mamelle, souvent d''origine bactérienne'),
(2, 'Fièvre aphteuse', 'Maladie virale très contagieuse'),
(3, 'Parasitisme intestinal', 'Infestation par des vers gastro-intestinaux'),
(4, 'Boiterie', 'Problème locomoteur, souvent lié aux onglons'),
(5, 'Fièvre de lait (hypocalcémie)', 'Trouble métabolique post-vêlage');

-- ============================================
-- MALADIE_MEDICAMENT (liaison)
-- ============================================
INSERT INTO maladie_medicament (maladie_id, medicament_id) VALUES
(1, 1), (1, 2),
(2, 4),
(3, 3),
(4, 4),
(5, 5);

INSERT INTO vache (id, numero_identification, nom, date_naissance, race) VALUES
(1, 'FR001234567', 'Marguerite', '2021-04-12', 'Holstein'),
(2, 'FR001234568', 'Blanchette', '2020-09-03', 'Montbéliarde'),
(3, 'FR001234569', 'Rosette',    '2022-01-20', 'Normande'),
(4, 'FR001234570', 'Noiraude',   '2019-11-15', 'Holstein'),
(5, 'FR001234571', 'Fleurette',  '2021-06-30', 'Montbéliarde');
-- ============================================
-- VACHE_HISTORIQUE_SANTE
-- (statut_id : 1 = Saine, 2 = Malade, 3 = En_traitement)
-- ============================================
INSERT INTO vache_historique_sante (vache_id, statut_id, date_debut, date_fin) VALUES
(1, 1, '2026-01-01', '2026-03-17'),   -- Saine
(1, 3, '2026-03-18', '2026-03-25'),   -- En_traitement (démarre direct, plus de "Malade")
(1, 1, '2026-03-26', NULL),           -- Saine (retour après traitement)

(2, 1, '2026-01-01', NULL),           -- Saine, jamais malade

(3, 1, '2026-01-01', '2026-05-01'),   -- Saine
(3, 3, '2026-05-02', NULL),           -- En_traitement (en cours)

(4, 1, '2026-01-01', NULL),           -- Saine

(5, 1, '2026-01-01', '2026-06-10'),   -- Saine
(5, 3, '2026-06-11', NULL);           -- En_traitement (en cours)

-- ============================================
-- EVENEMENT_SANTE
-- ============================================
INSERT INTO evenement_sante (id, vache_id, maladie_id, date_evenement, description, nbr_medicament) VALUES
(1, 1, 1, '2026-03-15', 'Mammite clinique détectée sur quartier arrière gauche', 1),
(2, 3, 3, '2026-05-02', 'Vers détectés lors du contrôle de routine', 1),
(3, 5, 1, '2026-06-11', 'Mammite subclinique confirmée par CMT', 2);

-- ============================================
-- TRAITEMENT_SANTE
-- ============================================
INSERT INTO traitement_sante (evenement_sante_id, medicament_id, dose, unite, duree_traitement, delai_attente_j, date_debut, date_fin) VALUES
(1, 1, 10.00, 'ml', 5, 3, '2026-03-18', '2026-03-22'),
(2, 3, 5.00, 'ml', 1, 21, '2026-05-02', '2026-05-02'),
(3, 1, 10.00, 'ml', 5, 3, '2026-06-11', '2026-06-15'),
(3, 4, 20.00, 'ml', 3, 5, '2026-06-11', '2026-06-13');
