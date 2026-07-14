--
-- PostgreSQL database dump
--

-- Dumped from database version 16.2
-- Dumped by pg_dump version 16.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: ref_phase_lactation; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.ref_phase_lactation VALUES (1, 'Tarie', 301, 365);
INSERT INTO public.ref_phase_lactation VALUES (2, 'Lactation haute', 0, 60);
INSERT INTO public.ref_phase_lactation VALUES (3, 'Lactation basse', 181, 300);
INSERT INTO public.ref_phase_lactation VALUES (4, 'Lactation moyenne', 61, 180);


--
-- Data for Name: ref_statut_sante; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.ref_statut_sante VALUES (1, 'Saine');
INSERT INTO public.ref_statut_sante VALUES (2, 'Malade');
INSERT INTO public.ref_statut_sante VALUES (3, 'En_traitement');


--
-- Data for Name: ration; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.ration VALUES (1, 'Ration lactation haute', 2, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.ration VALUES (2, 'Ration lactation moyenne', 4, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.ration VALUES (3, 'Ration lactation basse', 3, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.ration VALUES (4, 'Ration tarie', 1, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO public.ration VALUES (5, 'Ration haute production', NULL, 25.0, NULL, NULL, NULL, NULL, 1);
INSERT INTO public.ration VALUES (6, 'Ration basse production', NULL, NULL, 15.0, NULL, NULL, NULL, 1);
INSERT INTO public.ration VALUES (7, 'Ration BCS faible (maigre)', NULL, NULL, NULL, NULL, 2.49, NULL, 2);
INSERT INTO public.ration VALUES (8, 'Ration BCS lev (obSse)', NULL, NULL, NULL, 4.01, NULL, NULL, 2);
INSERT INTO public.ration VALUES (9, 'Ration convalescence (malade)', NULL, NULL, NULL, NULL, NULL, 2, 3);
INSERT INTO public.ration VALUES (10, 'Ration convalescence (traitement)', NULL, NULL, NULL, NULL, NULL, 3, 3);


--
-- Data for Name: ref_race; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.ref_race VALUES (1, 'prim_holstein', 'Prim''Holstein');
INSERT INTO public.ref_race VALUES (2, 'normande', 'Normande');
INSERT INTO public.ref_race VALUES (3, 'montbeliarde', 'Montbliarde');
INSERT INTO public.ref_race VALUES (4, 'charolaise', 'Charolaise');
INSERT INTO public.ref_race VALUES (5, 'TEST_HOL', 'Test Holstein');


--
-- Data for Name: vache; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.vache VALUES (1, 'V001_TEST', 1, '2021-01-15', 550.0, NULL, 3.50, NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.vache VALUES (2, 'V002_TEST', 2, '2020-06-10', 600.0, NULL, 2.80, NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.vache VALUES (3, 'V003_TEST', 3, '2022-03-20', 480.0, NULL, 4.80, NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.vache VALUES (4, 'V004_TEST', 1, '2019-11-05', 620.0, NULL, 3.10, NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.vache VALUES (5, 'TEST-001', 5, '2022-01-10', 545.0, NULL, 3.25, NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.vache VALUES (6, 'FR001234567', 1, '2021-04-12', NULL, NULL, NULL, NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.vache VALUES (7, 'FR001234568', 3, '2020-09-03', NULL, NULL, NULL, NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.vache VALUES (8, 'FR001234569', 2, '2022-01-20', NULL, NULL, NULL, NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.vache VALUES (9, 'FR001234570', 1, '2019-11-15', NULL, NULL, NULL, NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.vache VALUES (10, 'FR001234571', 3, '2021-06-30', NULL, NULL, NULL, NULL, '2026-07-14 12:50:36.637441+03');


--
-- Data for Name: affectation_ration_vache; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ref_niveau_alerte; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.ref_niveau_alerte VALUES (1, 'urgent', 'Urgent', 1);
INSERT INTO public.ref_niveau_alerte VALUES (2, 'attention', 'Attention', 2);
INSERT INTO public.ref_niveau_alerte VALUES (3, 'info', 'Info', 3);


--
-- Data for Name: alerte; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.alerte VALUES (1, 1, 'stock_aliment_bas_3', 'Stock insuffisant  Concentr', 'Stock actuel : 0 kg, seuil : 30.00 kg.', NULL, false, '2026-07-14 12:50:53.880147+03');
INSERT INTO public.alerte VALUES (2, 1, 'vaccin_en_retard', 'Vaccin en retard  V001_TEST', 'Vaccin Vaccin FiSvre Aphteuse (Primo) en retard de 20 jour(s) (rappel prevu le 2026-06-24).', 1, false, '2026-07-14 12:50:54.172747+03');
INSERT INTO public.alerte VALUES (3, 2, 'vaccin_prioritaire', 'Vaccin prioritaire dans 5j  V002_TEST', 'Vaccin Rhinotrachite Infectieuse Bovine (IBR)  prochain rappel le 2026-07-19.', 2, false, '2026-07-14 12:50:54.209424+03');
INSERT INTO public.alerte VALUES (4, 3, 'rappel_vaccin', 'Rappel vaccin  V003_TEST', 'Vaccin Vaccin Charbon Symptomatique  prochain rappel le 2026-09-17 (dans 65 jours).', 3, false, '2026-07-14 12:50:54.240253+03');
INSERT INTO public.alerte VALUES (5, 1, 'rappel_velage', 'Rappel velage  V004_TEST', 'Velage prevu le 2026-08-13  dans 30 jour(s) (IA enregistree le 2025-11-06).', 4, false, '2026-07-14 12:50:54.322022+03');
INSERT INTO public.alerte VALUES (6, 2, 'rappel_velage', 'Rappel velage  V001_TEST', 'Velage prevu le 2026-09-12  dans 60 jour(s) (IA enregistree le 2025-12-06).', 1, false, '2026-07-14 12:50:54.346321+03');
INSERT INTO public.alerte VALUES (7, 2, 'bcs_hors_plage', 'BCS hors plage  V003_TEST', 'Score BCS de 4.80 en dehors de la plage recommandee (2.5 - 4.0).', 3, false, '2026-07-14 12:51:06.426113+03');
INSERT INTO public.alerte VALUES (8, 2, 'baisse_production', 'Baisse prod. Lait  V001_TEST', 'Baisse critique de production pour la vache V001_TEST : 5.00L saisis (Seuil critique a 28.000L).', 1, false, '2026-07-14 16:20:26.682022+03');


--
-- Data for Name: ref_type_aliment; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.ref_type_aliment VALUES (1, 'foin', 'Foin');
INSERT INTO public.ref_type_aliment VALUES (2, 'ensilage', 'Ensilage');
INSERT INTO public.ref_type_aliment VALUES (3, 'concentre', 'Concentr');
INSERT INTO public.ref_type_aliment VALUES (4, 'mineral', 'Minral');


--
-- Data for Name: aliment; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.aliment VALUES (1, 'Foin', 1, 0.500, 45.00, 50.00, 1000.00);
INSERT INTO public.aliment VALUES (3, 'Concentre', 3, 1.050, 110.00, 30.00, 1500.00);
INSERT INTO public.aliment VALUES (2, 'Ensilage', 2, 0.800, 60.00, 100.00, 400.00);


--
-- Data for Name: maladie; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.maladie VALUES (1, 'Mammite', 'Inflammation de la mamelle, souvent d''origine bactrienne');
INSERT INTO public.maladie VALUES (2, 'FiSvre aphteuse', 'Maladie virale trSs contagieuse');
INSERT INTO public.maladie VALUES (3, 'Parasitisme intestinal', 'Infestation par des vers gastro-intestinaux');
INSERT INTO public.maladie VALUES (4, 'Boiterie', 'ProblSme locomoteur, souvent li aux onglons');
INSERT INTO public.maladie VALUES (5, 'FiSvre de lait (hypocalcmie)', 'Trouble mtabolique post-vlage');


--
-- Data for Name: evenement_sante; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.evenement_sante VALUES (1, 6, 1, '2026-03-15', 'Mammite clinique dtecte sur quartier arriSre gauche', '2026-07-14 12:50:36.637441+03');
INSERT INTO public.evenement_sante VALUES (2, 8, 3, '2026-05-02', 'Vers dtects lors du contrle de routine', '2026-07-14 12:50:36.637441+03');
INSERT INTO public.evenement_sante VALUES (3, 10, 1, '2026-06-11', 'Mammite subclinique confirme par CMT', '2026-07-14 12:50:36.637441+03');


--
-- Data for Name: protocole_vaccin; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.protocole_vaccin VALUES (1, 'Rhinotrachite Infectieuse Bovine (IBR)', 150, 360, 365);
INSERT INTO public.protocole_vaccin VALUES (2, 'Vaccin Charbon Symptomatique', 90, 180, 365);
INSERT INTO public.protocole_vaccin VALUES (3, 'Vaccin FiSvre Aphteuse (Primo)', 60, 120, 180);


--
-- Data for Name: historique_vaccin; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.historique_vaccin VALUES (1, 1, 3, '2025-12-26', 'IA frache');
INSERT INTO public.historique_vaccin VALUES (2, 2, 1, '2025-07-19', 'IA congele');
INSERT INTO public.historique_vaccin VALUES (3, 3, 2, '2025-09-17', 'IA frache');


--
-- Data for Name: ref_statut_lactation; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.ref_statut_lactation VALUES (1, 'active', 'Active');
INSERT INTO public.ref_statut_lactation VALUES (2, 'terminee', 'Termine');


--
-- Data for Name: lactation; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.lactation VALUES (1, 2, 1, '2026-07-04', NULL, 1, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.lactation VALUES (2, 5, 1, '2026-01-01', NULL, 1, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.lactation VALUES (3, 1, 1, '2026-07-14', NULL, 1, '2026-07-14 16:03:40.700198+03');
INSERT INTO public.lactation VALUES (4, 3, 1, '2026-07-14', NULL, 1, '2026-07-14 16:28:26.448413+03');


--
-- Data for Name: medicament; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.medicament VALUES (1, 'Amoxicilline 15%');
INSERT INTO public.medicament VALUES (2, 'Oxytetracycline LA');
INSERT INTO public.medicament VALUES (3, 'Ivermectine');
INSERT INTO public.medicament VALUES (4, 'Anti-inflammatoire Meloxicam');
INSERT INTO public.medicament VALUES (5, 'Vitamine B12');


--
-- Data for Name: maladie_medicament; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.maladie_medicament VALUES (1, 1);
INSERT INTO public.maladie_medicament VALUES (1, 2);
INSERT INTO public.maladie_medicament VALUES (2, 4);
INSERT INTO public.maladie_medicament VALUES (3, 3);
INSERT INTO public.maladie_medicament VALUES (4, 4);
INSERT INTO public.maladie_medicament VALUES (5, 5);


--
-- Data for Name: medicament_fille; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.medicament_fille VALUES (1, 1, 10.00, 'ml', 3, 8, 1000.00);
INSERT INTO public.medicament_fille VALUES (2, 1, 20.00, 'ml', 3, 8, 1800.00);
INSERT INTO public.medicament_fille VALUES (3, 2, 15.00, 'ml', 4, 15, 900.80);
INSERT INTO public.medicament_fille VALUES (4, 3, 5.00, 'ml', 0, 21, 1500.00);
INSERT INTO public.medicament_fille VALUES (5, 4, 20.00, 'ml', 2, 5, 7000.25);
INSERT INTO public.medicament_fille VALUES (6, 5, 10.00, 'ml', 0, 0, 4000.00);


--
-- Data for Name: ref_role_utilisateur; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.ref_role_utilisateur VALUES (1, 'admin', 'Administrateur');
INSERT INTO public.ref_role_utilisateur VALUES (2, 'employe', 'Employ');


--
-- Data for Name: utilisateur; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.utilisateur VALUES (1, 'Admin Test', 'admin@laitgo.mg', 1, '$2y$10$k3CgylH3wYI63CdKC.CpNO0i3RD0ml4ZRYcz7dKt.Q9P7qcJM9uVa', true, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.utilisateur VALUES (2, 'Employe Test', 'employe@laitgo.mg', 2, '$2y$10$T3bqAFpDJheUhPSV1w/T9.psStGYQsJUhWglT585Kp3fvd5mqwnzC', true, '2026-07-14 12:50:36.637441+03');


--
-- Data for Name: mouvement_aliment; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.mouvement_aliment VALUES (1, 1, 'entree', 500.00, '2026-06-14', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (2, 2, 'entree', 500.00, '2026-06-14', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (3, 1, 'sortie', 470.00, '2026-07-14', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (4, 2, 'sortie', 420.00, '2026-07-14', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (5, 2, 'entree', 920.00, '2026-06-04', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (6, 1, 'entree', 610.00, '2026-06-03', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (7, 2, 'entree', 840.00, '2026-02-04', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (8, 1, 'sortie', 240.00, '2026-05-20', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (9, 1, 'sortie', 235.00, '2026-04-20', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (10, 1, 'entree', 590.00, '2026-05-03', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (11, 2, 'sortie', 290.00, '2026-04-22', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (12, 1, 'entree', 520.00, '2026-01-03', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (13, 1, 'entree', 570.00, '2026-04-03', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (14, 2, 'entree', 860.00, '2026-03-04', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (15, 1, 'sortie', 230.00, '2026-03-20', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (16, 2, 'entree', 820.00, '2026-01-04', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (17, 1, 'sortie', 225.00, '2026-02-20', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (18, 2, 'sortie', 310.00, '2026-06-22', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (19, 2, 'entree', 900.00, '2026-05-04', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (20, 1, 'sortie', 250.00, '2026-06-20', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (21, 1, 'sortie', 220.00, '2026-01-20', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (22, 2, 'sortie', 300.00, '2026-05-22', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (23, 2, 'sortie', 280.00, '2026-03-22', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (24, 1, 'entree', 560.00, '2026-03-03', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (25, 1, 'entree', 540.00, '2026-02-03', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (26, 2, 'entree', 880.00, '2026-04-04', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (27, 2, 'sortie', 270.00, '2026-02-22', NULL, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.mouvement_aliment VALUES (28, 2, 'sortie', 260.00, '2026-01-22', NULL, '2026-07-14 12:50:36.637441+03');


--
-- Data for Name: production; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.production VALUES (8, 3, 1, '2026-07-13', 35.00, 20.00, 15.00, 0.00, '2026-07-14 16:03:40.771008+03', NULL);
INSERT INTO public.production VALUES (1, 1, 2, '2026-07-14', 30.00, 15.00, 15.00, 0.00, '2026-07-14 12:50:36.637441+03', NULL);
INSERT INTO public.production VALUES (10, 3, 1, '2026-07-14', 5.00, 5.00, 0.00, 0.00, '2026-07-14 16:21:02.533142+03', NULL);
INSERT INTO public.production VALUES (11, 4, 3, '2026-07-14', 250.00, 150.00, 100.00, 95.00, '2026-07-14 16:28:26.450427+03', NULL);


--
-- Data for Name: ration_aliment; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.ration_aliment VALUES (1, 3, 1, 3.00);
INSERT INTO public.ration_aliment VALUES (2, 3, 3, 1.00);
INSERT INTO public.ration_aliment VALUES (3, 4, 1, 4.00);
INSERT INTO public.ration_aliment VALUES (4, 2, 2, 3.00);
INSERT INTO public.ration_aliment VALUES (5, 1, 3, 2.00);
INSERT INTO public.ration_aliment VALUES (6, 1, 1, 5.00);
INSERT INTO public.ration_aliment VALUES (7, 2, 3, 1.50);
INSERT INTO public.ration_aliment VALUES (8, 1, 2, 3.00);
INSERT INTO public.ration_aliment VALUES (9, 4, 2, 2.00);
INSERT INTO public.ration_aliment VALUES (10, 2, 1, 4.00);
INSERT INTO public.ration_aliment VALUES (11, 3, 2, 2.00);


--
-- Data for Name: ref_produit; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.ref_produit VALUES (1, 'LAIT', 'Lait', 'L');
INSERT INTO public.ref_produit VALUES (2, 'ENGRAIS', 'Engrais', 'kg');
INSERT INTO public.ref_produit VALUES (3, 'VIANDE', 'Viande', 'kg');
INSERT INTO public.ref_produit VALUES (4, 'VACHE', 'Vache', 'unit');
INSERT INTO public.ref_produit VALUES (5, 'VEAU', 'Veau', 'unit');


--
-- Data for Name: ref_statut_lactation_vache; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.ref_statut_lactation_vache VALUES (1, 'Tarie');
INSERT INTO public.ref_statut_lactation_vache VALUES (2, 'En_lactation');


--
-- Data for Name: ref_statut_repro; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.ref_statut_repro VALUES (1, 'En_chaleur');
INSERT INTO public.ref_statut_repro VALUES (2, 'Gestante');
INSERT INTO public.ref_statut_repro VALUES (3, 'Inseminee');
INSERT INTO public.ref_statut_repro VALUES (4, 'Vide');


--
-- Data for Name: ref_statut_vie; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.ref_statut_vie VALUES (1, 'Vendue');
INSERT INTO public.ref_statut_vie VALUES (2, 'Vache_active');
INSERT INTO public.ref_statut_vie VALUES (3, 'Reformee');
INSERT INTO public.ref_statut_vie VALUES (4, 'Genisse');
INSERT INTO public.ref_statut_vie VALUES (5, 'Veau');
INSERT INTO public.ref_statut_vie VALUES (6, 'Morte');


--
-- Data for Name: ref_type_ia; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.ref_type_ia VALUES (1, 'IA sexe');
INSERT INTO public.ref_type_ia VALUES (2, 'IA frache');
INSERT INTO public.ref_type_ia VALUES (3, 'IA congele');
INSERT INTO public.ref_type_ia VALUES (4, 'IA aprSs dtection de chaleur');
INSERT INTO public.ref_type_ia VALUES (5, 'IA  heure fixe');
INSERT INTO public.ref_type_ia VALUES (6, 'IA avec synchronisation (Ovsynch)');


--
-- Data for Name: reproduction; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.reproduction VALUES (1, 4, '2025-11-06', true, '2025-12-26', NULL, NULL, 'gestante', NULL, NULL, NULL);
INSERT INTO public.reproduction VALUES (2, 1, '2025-12-06', true, '2026-01-25', NULL, NULL, 'gestante', NULL, NULL, NULL);


--
-- Data for Name: traitement_sante; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.traitement_sante VALUES (1, 1, 1, 5, '2026-03-18', '2026-03-22', 1, 3);
INSERT INTO public.traitement_sante VALUES (2, 3, 1, 5, '2026-06-11', '2026-06-15', 1, 3);
INSERT INTO public.traitement_sante VALUES (3, 3, 5, 3, '2026-06-11', '2026-06-13', 1, 5);
INSERT INTO public.traitement_sante VALUES (4, 2, 4, 1, '2026-05-02', '2026-05-02', 1, 21);


--
-- Data for Name: vache_historique_lactation; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.vache_historique_lactation VALUES (1, 1, 2, '2023-01-01', NULL);
INSERT INTO public.vache_historique_lactation VALUES (2, 2, 2, '2023-01-01', NULL);
INSERT INTO public.vache_historique_lactation VALUES (3, 3, 2, '2023-01-01', NULL);
INSERT INTO public.vache_historique_lactation VALUES (4, 4, 2, '2023-01-01', NULL);


--
-- Data for Name: vache_historique_repro; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.vache_historique_repro VALUES (2, 2, 4, '2023-01-01', NULL);
INSERT INTO public.vache_historique_repro VALUES (3, 3, 4, '2023-01-01', NULL);
INSERT INTO public.vache_historique_repro VALUES (4, 4, 4, '2023-01-01', '2026-07-13');
INSERT INTO public.vache_historique_repro VALUES (5, 4, 2, '2026-07-13', NULL);
INSERT INTO public.vache_historique_repro VALUES (1, 1, 4, '2023-01-01', '2026-07-13');
INSERT INTO public.vache_historique_repro VALUES (6, 1, 2, '2026-07-13', NULL);


--
-- Data for Name: vache_historique_sante; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.vache_historique_sante VALUES (1, 1, 1, '2023-01-01', NULL);
INSERT INTO public.vache_historique_sante VALUES (2, 2, 1, '2023-01-01', NULL);
INSERT INTO public.vache_historique_sante VALUES (3, 3, 1, '2023-01-01', NULL);
INSERT INTO public.vache_historique_sante VALUES (4, 4, 1, '2023-01-01', NULL);
INSERT INTO public.vache_historique_sante VALUES (5, 6, 3, '2026-03-18', '2026-03-25');
INSERT INTO public.vache_historique_sante VALUES (6, 6, 1, '2026-01-01', '2026-03-17');
INSERT INTO public.vache_historique_sante VALUES (7, 6, 1, '2026-03-26', NULL);
INSERT INTO public.vache_historique_sante VALUES (8, 7, 1, '2026-01-01', NULL);
INSERT INTO public.vache_historique_sante VALUES (9, 8, 3, '2026-05-02', NULL);
INSERT INTO public.vache_historique_sante VALUES (10, 8, 1, '2026-01-01', '2026-05-01');
INSERT INTO public.vache_historique_sante VALUES (11, 9, 1, '2026-01-01', NULL);
INSERT INTO public.vache_historique_sante VALUES (12, 10, 3, '2026-06-11', NULL);
INSERT INTO public.vache_historique_sante VALUES (13, 10, 1, '2026-01-01', '2026-06-10');


--
-- Data for Name: vache_historique_vie; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.vache_historique_vie VALUES (1, 1, 2, '2023-01-01', NULL);
INSERT INTO public.vache_historique_vie VALUES (2, 2, 2, '2023-01-01', NULL);
INSERT INTO public.vache_historique_vie VALUES (3, 3, 2, '2023-01-01', NULL);
INSERT INTO public.vache_historique_vie VALUES (4, 4, 2, '2023-01-01', NULL);


--
-- Data for Name: vente; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.vente VALUES (1, '2026-07-14', 1, 25.00, 1500.00, 1, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.vente VALUES (2, '2026-04-25', 1, 340.00, 1670.00, 1, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.vente VALUES (3, '2026-01-25', 1, 300.00, 1600.00, 1, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.vente VALUES (4, '2026-05-25', 1, 360.00, 1700.00, 1, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.vente VALUES (5, '2026-06-25', 1, 390.00, 1720.00, 1, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.vente VALUES (6, '2026-02-25', 1, 320.00, 1620.00, 1, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.vente VALUES (7, '2026-03-25', 1, 350.00, 1650.00, 1, '2026-07-14 12:50:36.637441+03');
INSERT INTO public.vente VALUES (8, '2026-07-14', 1, 17.00, 3000.00, 2, '2026-07-14 16:05:39.079083+03');
INSERT INTO public.vente VALUES (9, '2026-07-14', 1, 10.00, 3000.00, 2, '2026-07-14 16:10:10.224633+03');
INSERT INTO public.vente VALUES (10, '2026-07-14', 1, 20.00, 3000.00, 2, '2026-07-14 16:13:14.759614+03');
INSERT INTO public.vente VALUES (11, '2026-07-14', 1, 12.00, 3000.00, 2, '2026-07-14 16:14:38.11301+03');
INSERT INTO public.vente VALUES (12, '2026-07-14', 1, 200.00, 3000.00, 2, '2026-07-14 16:29:24.275858+03');
INSERT INTO public.vente VALUES (13, '2026-07-14', 2, 100.00, 500.00, 2, '2026-07-14 16:30:07.574055+03');


--
-- Name: affectation_ration_vache_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.affectation_ration_vache_id_seq', 1, false);


--
-- Name: alerte_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.alerte_id_seq', 8, true);


--
-- Name: aliment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.aliment_id_seq', 3, true);


--
-- Name: evenement_sante_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.evenement_sante_id_seq', 3, true);


--
-- Name: historique_vaccin_id_historique_vaccin_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.historique_vaccin_id_historique_vaccin_seq', 3, true);


--
-- Name: lactation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.lactation_id_seq', 4, true);


--
-- Name: maladie_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.maladie_id_seq', 5, true);


--
-- Name: medicament_fille_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.medicament_fille_id_seq', 6, true);


--
-- Name: medicament_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.medicament_id_seq', 5, true);


--
-- Name: mouvement_aliment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.mouvement_aliment_id_seq', 28, true);


--
-- Name: production_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.production_id_seq', 11, true);


--
-- Name: protocole_vaccin_id_protocole_vaccin_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.protocole_vaccin_id_protocole_vaccin_seq', 3, true);


--
-- Name: ration_aliment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ration_aliment_id_seq', 11, true);


--
-- Name: ration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ration_id_seq', 10, true);


--
-- Name: ref_niveau_alerte_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ref_niveau_alerte_id_seq', 3, true);


--
-- Name: ref_phase_lactation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ref_phase_lactation_id_seq', 4, true);


--
-- Name: ref_produit_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ref_produit_id_seq', 5, true);


--
-- Name: ref_race_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ref_race_id_seq', 5, true);


--
-- Name: ref_role_utilisateur_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ref_role_utilisateur_id_seq', 2, true);


--
-- Name: ref_statut_lactation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ref_statut_lactation_id_seq', 2, true);


--
-- Name: ref_statut_lactation_vache_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ref_statut_lactation_vache_id_seq', 2, true);


--
-- Name: ref_statut_repro_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ref_statut_repro_id_seq', 4, true);


--
-- Name: ref_statut_sante_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ref_statut_sante_id_seq', 3, true);


--
-- Name: ref_statut_vie_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ref_statut_vie_id_seq', 6, true);


--
-- Name: ref_type_aliment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ref_type_aliment_id_seq', 4, true);


--
-- Name: ref_type_ia_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ref_type_ia_id_seq', 6, true);


--
-- Name: reproduction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.reproduction_id_seq', 2, true);


--
-- Name: traitement_sante_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.traitement_sante_id_seq', 4, true);


--
-- Name: utilisateur_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.utilisateur_id_seq', 2, true);


--
-- Name: vache_historique_lactation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.vache_historique_lactation_id_seq', 4, true);


--
-- Name: vache_historique_repro_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.vache_historique_repro_id_seq', 6, true);


--
-- Name: vache_historique_sante_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.vache_historique_sante_id_seq', 13, true);


--
-- Name: vache_historique_vie_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.vache_historique_vie_id_seq', 4, true);


--
-- Name: vache_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.vache_id_seq', 10, true);


--
-- Name: vente_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.vente_id_seq', 13, true);


--
-- PostgreSQL database dump complete
--


