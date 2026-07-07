-- ============================================================
-- DONNÉES DE TEST — Module Alertes
-- À exécuter dans psql sur la base laitgo
-- Simule des alertes depuis chaque module
-- ============================================================

-- ============================================================
-- 1. VACHES DE TEST
-- ============================================================
INSERT INTO vache (numero_boucle, id_race, date_naissance, poids_kg, score_bcs)
VALUES
    ('V001_TEST', (SELECT id FROM ref_race WHERE code = 'prim_holstein'), '2021-01-15', 550.0, 3.50),
    ('V002_TEST', (SELECT id FROM ref_race WHERE code = 'normande'),      '2020-06-10', 600.0, 2.80),
    ('V003_TEST', (SELECT id FROM ref_race WHERE code = 'montbeliarde'),  '2022-03-20', 480.0, 4.80),
    ('V004_TEST', (SELECT id FROM ref_race WHERE code = 'prim_holstein'), '2019-11-05', 620.0, 3.10)
ON CONFLICT (numero_boucle) DO NOTHING;

-- Statuts vie
INSERT INTO vache_historique_vie (vache_id, statut_id, date_debut)
SELECT v.id, s.id, '2023-01-01'
FROM vache v, ref_statut_vie s
WHERE v.numero_boucle IN ('V001_TEST','V002_TEST','V003_TEST','V004_TEST')
  AND s.libelle = 'Vache_active'
ON CONFLICT DO NOTHING;

-- Statuts repro
INSERT INTO vache_historique_repro (vache_id, statut_id, date_debut)
SELECT v.id, s.id, '2023-01-01'
FROM vache v, ref_statut_repro s
WHERE v.numero_boucle IN ('V001_TEST','V002_TEST','V003_TEST','V004_TEST')
  AND s.libelle = 'Vide'
ON CONFLICT DO NOTHING;

-- Statuts lactation
INSERT INTO vache_historique_lactation (vache_id, statut_id, date_debut)
SELECT v.id, s.id, '2023-01-01'
FROM vache v, ref_statut_lactation_vache s
WHERE v.numero_boucle IN ('V001_TEST','V002_TEST','V003_TEST','V004_TEST')
  AND s.libelle = 'En_lactation'
ON CONFLICT DO NOTHING;

-- Statuts santé
INSERT INTO vache_historique_sante (vache_id, statut_id, date_debut)
SELECT v.id, s.id, '2023-01-01'
FROM vache v, ref_statut_sante s
WHERE v.numero_boucle IN ('V001_TEST','V002_TEST','V003_TEST','V004_TEST')
  AND s.libelle = 'Saine'
ON CONFLICT DO NOTHING;

-- ============================================================
-- 2. MODULE VACCINATION — 3 types d'alertes
-- Vaccin en retard (urgent) + prioritaire (attention) + rappel (info)
-- ============================================================

-- V001 : vaccin Fièvre Aphteuse fait il y a 200 jours → EN RETARD (rappel=180j)
INSERT INTO historique_vaccin (vache_id, id_protocole_vaccin, date_vaccination, type_injection)
SELECT v.id, p.id_protocole_vaccin, CURRENT_DATE - INTERVAL '200 days', 'IA fraîche'
FROM vache v, protocole_vaccin p
WHERE v.numero_boucle = 'V001_TEST'
  AND p.nom_vaccin = 'Vaccin Fièvre Aphteuse (Primo)';

-- V002 : vaccin IBR fait il y a 360 jours → PRIORITAIRE dans 5 jours (rappel=365j)
INSERT INTO historique_vaccin (vache_id, id_protocole_vaccin, date_vaccination, type_injection)
SELECT v.id, p.id_protocole_vaccin, CURRENT_DATE - INTERVAL '360 days', 'IA congelée'
FROM vache v, protocole_vaccin p
WHERE v.numero_boucle = 'V002_TEST'
  AND p.nom_vaccin = 'Rhinotrachéite Infectieuse Bovine (IBR)';

-- V003 : vaccin Charbon fait il y a 300 jours → RAPPEL INFO (rappel=365j, dans 65 jours)
INSERT INTO historique_vaccin (vache_id, id_protocole_vaccin, date_vaccination, type_injection)
SELECT v.id, p.id_protocole_vaccin, CURRENT_DATE - INTERVAL '300 days', 'IA fraîche'
FROM vache v, protocole_vaccin p
WHERE v.numero_boucle = 'V003_TEST'
  AND p.nom_vaccin = 'Vaccin Charbon Symptomatique';

-- ============================================================
-- 3. MODULE REPRODUCTION — Rappel vêlage
-- V004 gestante depuis 250 jours → vêlage dans 30 jours (urgent)
-- V001 gestante depuis 220 jours → vêlage dans 60 jours (attention)
-- ============================================================

-- Mettre V004 en statut gestante
UPDATE vache_historique_repro
SET date_fin = CURRENT_DATE - INTERVAL '1 day'
WHERE vache_id = (SELECT id FROM vache WHERE numero_boucle = 'V004_TEST')
  AND date_fin IS NULL;

INSERT INTO vache_historique_repro (vache_id, statut_id, date_debut)
SELECT v.id, s.id, CURRENT_DATE - INTERVAL '1 day'
FROM vache v, ref_statut_repro s
WHERE v.numero_boucle = 'V004_TEST'
  AND s.libelle = 'Gestante';

-- Enregistrer IA de V004 : il y a 250 jours → vêlage prévu dans 30 jours
INSERT INTO reproduction (vache_id, date_ia, gestation_confirmee, date_confirmation_gest, statut_ia)
SELECT v.id, CURRENT_DATE - INTERVAL '250 days', true, CURRENT_DATE - INTERVAL '200 days', 'gestante'
FROM vache v
WHERE v.numero_boucle = 'V004_TEST';

-- Enregistrer IA de V001 : il y a 220 jours → vêlage prévu dans 60 jours
UPDATE vache_historique_repro
SET date_fin = CURRENT_DATE - INTERVAL '1 day'
WHERE vache_id = (SELECT id FROM vache WHERE numero_boucle = 'V001_TEST')
  AND date_fin IS NULL;

INSERT INTO vache_historique_repro (vache_id, statut_id, date_debut)
SELECT v.id, s.id, CURRENT_DATE - INTERVAL '1 day'
FROM vache v, ref_statut_repro s
WHERE v.numero_boucle = 'V001_TEST'
  AND s.libelle = 'Gestante';

INSERT INTO reproduction (vache_id, date_ia, gestation_confirmee, date_confirmation_gest, statut_ia)
SELECT v.id, CURRENT_DATE - INTERVAL '220 days', true, CURRENT_DATE - INTERVAL '170 days', 'gestante'
FROM vache v
WHERE v.numero_boucle = 'V001_TEST';

-- ============================================================
-- 4. MODULE ALIMENTATION — Stock sous le seuil
-- Foin : seuil=50kg, on met un stock de 30kg → alerte urgent
-- Ensilage : seuil=100kg, on met un stock de 80kg → alerte urgent
-- ============================================================

-- Entrée initiale de stock (nécessaire avant toute sortie)
INSERT INTO mouvement_aliment (aliment_id, type_mouvement, quantite_kg, date_mouvement, created_at)
SELECT a.id, 'entree', 500.00, CURRENT_DATE - INTERVAL '30 days', NOW()
FROM aliment a WHERE a.nom = 'Foin';

INSERT INTO mouvement_aliment (aliment_id, type_mouvement, quantite_kg, date_mouvement, created_at)
SELECT a.id, 'entree', 500.00, CURRENT_DATE - INTERVAL '30 days', NOW()
FROM aliment a WHERE a.nom = 'Ensilage';

-- Sortie qui fait passer sous le seuil pour Foin (500 - 470 = 30kg < seuil 50kg)
INSERT INTO mouvement_aliment (aliment_id, type_mouvement, quantite_kg, date_mouvement, created_at)
SELECT a.id, 'sortie', 470.00, CURRENT_DATE, NOW()
FROM aliment a WHERE a.nom = 'Foin';

-- Sortie qui fait passer sous le seuil pour Ensilage (500 - 420 = 80kg < seuil 100kg)
INSERT INTO mouvement_aliment (aliment_id, type_mouvement, quantite_kg, date_mouvement, created_at)
SELECT a.id, 'sortie', 420.00, CURRENT_DATE, NOW()
FROM aliment a WHERE a.nom = 'Ensilage';

-- ============================================================
-- 5. MODULE VENTE — Stock lait insuffisant
-- Production de 30L puis vente de 25L → stock restant 5L < seuil 50L
-- ============================================================

-- Créer une lactation pour V002
INSERT INTO lactation (vache_id, numero_lactation, date_debut, id_statut)
SELECT v.id, 1, CURRENT_DATE - INTERVAL '10 days',
       (SELECT id FROM ref_statut_lactation WHERE code = 'active')
FROM vache v WHERE v.numero_boucle = 'V002_TEST';

-- Production de 30L
INSERT INTO production (lactation_id, vache_id, date_production, quantite_litres,
                        quantite_matin, quantite_soir, quantite_restante)
SELECT l.id, l.vache_id, CURRENT_DATE, 30.00, 15.00, 15.00, 30.00
FROM lactation l
JOIN vache v ON v.id = l.vache_id
WHERE v.numero_boucle = 'V002_TEST'
  AND l.date_fin IS NULL;

-- Vente de 25L → stock restant = 5L (< seuil 50L → alerte attention)
INSERT INTO vente (date_vente, quantite_litres, prix_unitaire, created_at, created_by)
VALUES (CURRENT_DATE, 25.00, 1500.00, NOW(),
        (SELECT id FROM utilisateur WHERE email = 'admin@laitgo.mg'));

-- Mettre à jour quantite_restante après vente
UPDATE production
SET quantite_restante = 5.00
WHERE vache_id = (SELECT id FROM vache WHERE numero_boucle = 'V002_TEST')
  AND date_production = CURRENT_DATE;

-- ============================================================
-- 6. ALERTES DIRECTES EN BDD (pour tester l'affichage sans passer par les services)
-- Ces alertes simulent ce que les services auraient créé
-- ============================================================

DELETE FROM alerte WHERE titre LIKE '%TEST%';

INSERT INTO alerte (id_niveau, type_alerte, titre, description, vache_id, acquittee)
VALUES
-- URGENT — Vaccination
((SELECT id FROM ref_niveau_alerte WHERE code = 'urgent'),
 'vaccin_en_retard',
 'Vaccin en retard — V001_TEST',
 'Vaccin Fièvre Aphteuse (Primo) en retard de 20 jour(s) (rappel prévu le '
    || (CURRENT_DATE - INTERVAL '20 days')::text || ').',
 (SELECT id FROM vache WHERE numero_boucle = 'V001_TEST'),
 false),

-- URGENT — Alimentation
((SELECT id FROM ref_niveau_alerte WHERE code = 'urgent'),
 'stock_aliment_bas',
 'Stock insuffisant — Foin',
 'Stock actuel : 30 kg, seuil configuré : 50 kg.',
 null,
 false),

-- URGENT — Alimentation
((SELECT id FROM ref_niveau_alerte WHERE code = 'urgent'),
 'stock_aliment_bas',
 'Stock insuffisant — Ensilage',
 'Stock actuel : 80 kg, seuil configuré : 100 kg.',
 null,
 false),

-- ATTENTION — Vaccination
((SELECT id FROM ref_niveau_alerte WHERE code = 'attention'),
 'vaccin_prioritaire',
 'Vaccin prioritaire dans 5j — V002_TEST',
 'Vaccin Rhinotrachéite Infectieuse Bovine (IBR) — prochain rappel le '
    || (CURRENT_DATE + INTERVAL '5 days')::text || '.',
 (SELECT id FROM vache WHERE numero_boucle = 'V002_TEST'),
 false),

-- ATTENTION — Vente
((SELECT id FROM ref_niveau_alerte WHERE code = 'attention'),
 'stock_lait_bas',
 'Stock de lait insuffisant',
 'Stock restant après vente du ' || CURRENT_DATE::text || ' : 5 L (seuil critique : 50 L).',
 null,
 false),

-- ATTENTION — Reproduction
((SELECT id FROM ref_niveau_alerte WHERE code = 'attention'),
 'rappel_velage',
 'Rappel vêlage — V001_TEST',
 'Vêlage prévu le ' || (CURRENT_DATE + INTERVAL '60 days')::text
    || ' — dans 60 jour(s) (IA enregistrée le '
    || (CURRENT_DATE - INTERVAL '220 days')::text || ').',
 (SELECT id FROM vache WHERE numero_boucle = 'V001_TEST'),
 false),

-- INFO — Vaccination
((SELECT id FROM ref_niveau_alerte WHERE code = 'info'),
 'rappel_vaccin',
 'Rappel vaccin — V003_TEST',
 'Vaccin Charbon Symptomatique — prochain rappel le '
    || (CURRENT_DATE + INTERVAL '65 days')::text || ' (dans 65 jours).',
 (SELECT id FROM vache WHERE numero_boucle = 'V003_TEST'),
 false),

-- URGENT — Reproduction
((SELECT id FROM ref_niveau_alerte WHERE code = 'urgent'),
 'rappel_velage',
 'Rappel vêlage — V004_TEST',
 'Vêlage prévu le ' || (CURRENT_DATE + INTERVAL '30 days')::text
    || ' — dans 30 jour(s) (IA enregistrée le '
    || (CURRENT_DATE - INTERVAL '250 days')::text || ').',
 (SELECT id FROM vache WHERE numero_boucle = 'V004_TEST'),
 false),

-- ATTENTION — Traitement (alerte déjà acquittée pour tester affichage en bas)
((SELECT id FROM ref_niveau_alerte WHERE code = 'attention'),
 'traitement_en_cours',
 'Vache en traitement — V003_TEST [ACQUITTÉE]',
 'Test alerte acquittée — affichée en bas de liste.',
 (SELECT id FROM vache WHERE numero_boucle = 'V003_TEST'),
 true);

-- ============================================================
-- VÉRIFICATION FINALE
-- ============================================================
SELECT
    n.code      AS niveau,
    a.type_alerte,
    a.titre,
    a.acquittee,
    a.created_at::date AS date
FROM alerte a
JOIN ref_niveau_alerte n ON n.id = a.id_niveau
ORDER BY n.ordre, a.created_at DESC;