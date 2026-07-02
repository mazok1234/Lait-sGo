-- Insertion des maladies courantes
INSERT INTO maladie (id, nom, description) VALUES
(1, 'Mammite', 'Infection de la mamelle (lait caillé, mamelle chaude et gonflée). Critique pour le lait.'),
(2, 'Fièvre de lait', 'Hypocalcémie juste après le vêlage. La vache n''arrive plus à se lever.'),
(3, 'Boiterie / Piétin', 'Infection ou blessure au niveau des sabots. La vache boite et refuse de marcher.'),
(4, 'Parasitisme (Vers)', 'Infestation de vers intestinaux ou pulmonaires entraînant une baisse de production.');

-- Insertion des médicaments avec leurs délais d'attente par défaut
INSERT INTO medicament (id, nom, delai_attente_lait_defaut, delai_attente_viande_defaut) VALUES
(1, 'Cobactan (Antibiotique)', 3, 8),
(2, 'Finadyne (Anti-inflammatoire)', 1, 5),
(3, 'Calciton (Gluconate de Calcium)', 0, 0),
(4, 'Aluspray', 0, 0),
(5, 'Dectomax (Antiparasitaire)', 0, 35);

-- Liaison entre maladies et médicaments adaptés
INSERT INTO maladie_medicament (maladie_id, medicament_id) VALUES
(1, 1),
(1, 2),
(2, 3),
(3, 2),
(3, 4),
(4, 5);

INSERT INTO vache (numero_boucle, id_race, date_naissance, poids_kg, id_statut, mere_id, score_bcs, score_locomotion) 
VALUES
-- Une vache en lactation (Statut ID 1)
('FR1234567801', 1, '2020-03-15', 680.5, 1, NULL, 3.00, 1);


INSERT INTO ref_statut_vache (code, libelle) VALUES
('en_lactation', 'En lactation'),
('tarie',        'Tarie (période de repos avant vêlage)'),
('gestante',     'Gestante'),
('reformee',     'Réformée (sortie du troupeau)');