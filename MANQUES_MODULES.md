# BovinManager — Ce qui manque par module

Analyse basée sur le code actuel (`controller/`, `services/`, `base.sql`). Production et Reproduction ne sont pas listés : ces modules n'ont ni contrôleur ni service, seulement les tables SQL.

---

## Cheptel

**Manque**
- Le formulaire de création/édition de vache ne permet pas de saisir `score_bcs` ni `score_locomotion` (mis à `null` en dur dans `CheptelPageController`), alors que les colonnes existent en base — impossible d'exploiter la règle "BCS hors plage → ajuster la ration".
- Pas de vue généalogie/descendance : `mere_id` est stocké mais jamais affiché (aucune liste des filles d'une vache).
- Le changement de statut (`en_lactation`, `tarie`, `gestante`, `reformee`) est 100% manuel, aucune règle ne le déclenche automatiquement.
- Le type d'alerte `bcs_hors_plage` existe en base mais rien dans le code ne le génère.
- Pas de comparaison "production individuelle vs moyenne troupeau" pour objectiver une réforme (dépend aussi du module Production, absent).

**Alternatives**
- Ajouter `scoreBcs` / `scoreLocomotion` au formulaire, puis déclencher `AlerteService.envoyerAlerte("bcs_hors_plage", ...)` quand la valeur sort d'une plage définie.
- Ajouter un onglet "Filles" sur `vache-detail.html` via une requête `findByMereId`.
- Une fois Production disponible, afficher un indicateur "production 7j vs moyenne troupeau" sur la fiche vache pour appuyer la décision de réforme.

---

## Santé

**Manque**
- Aucun CRUD pour `Maladie` et `Medicament` (gérés uniquement via seed SQL) — impossible d'ajouter une maladie ou un médicament depuis l'interface.
- Pas de vue "vaches à problèmes récurrents", pourtant objectif affiché du module : `EvenementSanteRepository` ne contient aucune requête d'agrégation par vache/maladie.
- Les délais d'attente (`delai_attente_lait_defaut`, `delai_attente_j`) sont stockés mais jamais exploités pour bloquer/alerter une vente de lait pendant la période de traitement.
- Aucun rappel automatique de fin de traitement dans le module Alertes.

**Alternatives**
- Ajouter un CRUD simple Maladie/Médicament (même pattern que Race ou Type d'aliment).
- Ajouter une requête (ou vue SQL) type "vaches avec ≥2 épisodes de la même maladie sur 12 mois", affichée en dashboard Santé.
- Croiser `traitement_sante.date_fin + delai_attente_j` avec les ventes pour empêcher/alerter une vente trop précoce.

---

## Alimentation

**Manque**
- Le calcul de "stock bas" est refait à la main dans `AlimentController` (comparaison seuil/stock inline) au lieu de passer par l'entité `Alerte` : le type `stock_aliment_bas` existe en base mais n'est jamais créé — rien n'apparaît dans le module Alertes.
- Les vues SQL `v_ration_recommandee` et `v_ration_nutrition` (recommandation de ration selon la phase physiologique, calcul UFL/PDI/coût) existent en base mais ne sont utilisées nulle part côté Java.
- L'affectation ration → vache est entièrement manuelle : aucune vérification que la ration correspond au stade physiologique réel de la vache, ni que le stock suffit pour la durée de l'affectation.

**Alternatives**
- Appeler `AlerteService.envoyerAlerte("stock_aliment_bas", ...)` depuis `MouvementAlimentService`/`AlimentService` quand le stock passe sous le seuil.
- Exposer `v_ration_recommandee` via un service et pré-remplir le formulaire d'affectation avec la ration suggérée.
- Ajouter un contrôle de cohérence stock/ration à l'affectation (conso quotidienne × durée vs stock disponible).

---

## Alertes

**Manque**
- Seul le module Vaccination alimente réellement les alertes (`VaccinService`) ; les types `stock_lait_bas`, `rappel_velage`, `rappel_chaleur`, `stock_aliment_bas`, `bcs_hors_plage` existent en base mais aucun code ne les génère.
- Pas de création manuelle d'alerte depuis l'UI (seulement via `AlerteApiController`, aucun bouton dans une page).
- Aucune notification (email/push) : l'alerte reste passive tant que personne ne consulte le dashboard.

**Alternatives**
- Généraliser le pattern déjà utilisé pour Vaccination (`envoyerAlerte`) aux autres modules au fur et à mesure de leur implémentation.
- Ajouter un petit formulaire "créer une alerte manuelle" pour les cas non couverts par une règle automatique.
- Ajouter un job planifié (`@Scheduled`) qui recalcule les règles (stock, BCS...) une fois par jour, plutôt que de dépendre d'une consultation utilisateur pour déclencher le calcul.

---

## Vente

**Manque**
- Impossible de modifier ou supprimer une vente une fois enregistrée (aucune route edit/delete dans `VenteController`).
- L'utilisateur créateur est codé en dur (`utilisateurRepository.findById(1L)`) au lieu de dépendre de l'utilisateur connecté.
- La vue `v_offre_vente_jour` (écart production vs vente par jour) existe en base mais n'est exploitée nulle part — aucun graphique/rapport ne permet la décision "augmenter/réduire le troupeau".
- Le contrôle de stock avant vente (`productionRepository.getRemainingStock()`) ne peut en pratique jamais être alimenté, puisque le module Production n'a pas d'interface pour saisir la production.

**Alternatives**
- Ajouter edit/delete de vente (avec recalcul de `quantite_restante` sur les productions concernées).
- Brancher `VenteService` sur l'utilisateur réellement authentifié dès qu'une gestion de session/login existe.
- Construire un graphique "Production vs Vente (7/30 derniers jours)" à partir de `v_offre_vente_jour` pour matérialiser la décision d'ajustement du troupeau.
