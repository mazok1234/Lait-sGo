# BovinManager — Ce qui manque par module

Analyse basée sur le code actuel (`controller/`, `services/`, `base.sql`). Production et Lactation ne sont toujours pas listées comme module à part entière : `ProductionController` existe désormais, mais `Production.lactation` est une colonne obligatoire (`nullable = false`) que rien ne renseigne — tout enregistrement plante avec une violation de contrainte NOT NULL, et `Lactation` n'a ni contrôleur ni service. Ce point est traité dans le tableau de redondances plus bas plutôt que comme un module séparé.

---

## Cheptel

**Manque**
- Le formulaire de création/édition de vache ne permet pas de saisir `score_bcs` ni `score_locomotion` (mis à `null` en dur dans `CheptelPageController`), alors que les colonnes existent en base — impossible d'exploiter la règle "BCS hors plage → ajuster la ration".
- Pas de vue généalogie/descendance : `mere_id` est stocké mais jamais affiché (aucune liste des filles d'une vache).
- Le changement de statut (`en_lactation`, `tarie`, `gestante`, `reformee`) est 100% manuel, aucune règle ne le déclenche automatiquement (y compris depuis Reproduction, voir plus bas).
- Le type d'alerte `bcs_hors_plage` existe en base mais rien dans le code ne le génère.
- Pas de comparaison "production individuelle vs moyenne troupeau" pour objectiver une réforme (dépend du module Production, non fonctionnel).

**Alternatives**
- Ajouter `scoreBcs` / `scoreLocomotion` au formulaire, puis déclencher `AlerteService.envoyerAlerte("bcs_hors_plage", ...)` quand la valeur sort d'une plage définie.
- Ajouter un onglet "Filles" sur `vache-detail.html` via une requête `findByMereId`.
- Une fois Production/Lactation fonctionnels, afficher un indicateur "production 7j vs moyenne troupeau" sur la fiche vache pour appuyer la décision de réforme.

---

## Reproduction

**Manque**
- Le vêlage (`ReproductionService.enregistrerVelage`) ne déclenche aucune nouvelle `Lactation` : il se contente de mettre à jour `date_velage_reel`/`sexe_veau` sur la ligne `reproduction`. L'objectif affiché du module ("déclencher une nouvelle lactation") n'est pas implémenté alors que `Lactation`/`LactationRepository` existent déjà.
- Le vêlage ne met pas non plus à jour `vache.id_statut` : une vache reste "gestante" après avoir vêlé.
- Le "Suivi des chaleurs" du dashboard (`getChaleurs`, `getSuiviSemaine`) dépend entièrement de la table `vache_statut` (codes `chaleur`/`velage`), mais aucun code de l'application n'écrit jamais dans cette table (`VacheStatusRepository.save` n'est appelé nulle part) — la fonctionnalité reste vide tant que personne n'insère ces lignes manuellement en base.
- Les alertes de vêlage (`/reproduction/alertes`) sont calculées à la volée par `ReproductionAlerteService`, indépendamment du module Alertes central : les types `rappel_velage`/`rappel_chaleur` existent dans `ref_type_alerte` mais ne sont jamais utilisés. Ces alertes n'apparaissent donc jamais sur le dashboard `/alertes`, ne sont pas acquittables et sont recalculées à chaque visite au lieu d'être persistées.
- Le sexe du veau (`sexe_veau`) est enregistré mais rien n'exploite cette donnée pour anticiper le renouvellement du troupeau (objectif "sexe du veau (femelle) → anticiper le renouvellement").
- Pas de modification/suppression d'un vêlage une fois enregistré (contrairement aux IA, qui ont `update-statut`/`supprimer`).

**Alternatives**
- Faire en sorte que `enregistrerVelage` crée automatiquement une nouvelle `Lactation` (numéro suivant, `date_debut = dateVelageReel`, statut `active`) et fasse passer `vache.id_statut` à `en_lactation`, dans la même transaction.
- Décider du sort de `vache_statut` : soit l'alimenter réellement à chaque événement pertinent (chaleur détectée, vêlage), soit l'abandonner au profit des colonnes déjà présentes sur `reproduction` pour ne pas garder une deuxième source de vérité jamais peuplée.
- Brancher `ReproductionAlerteService.getAlertesVelage()` sur `AlerteService.envoyerAlerte("rappel_velage", ...)` pour que ces alertes vivent dans le système central (dashboard, acquittement, historique) au lieu d'un calcul parallèle non persisté.
- Ajouter un compteur "femelles nées en attente d'intégration" pour appuyer la décision de renouvellement du troupeau.

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
- Les vues SQL `v_ration_recommandee` et `v_ration_nutrition` (recommandation de ration selon la phase physiologique, calcul UFL/PDI/coût) existent en base mais ne sont utilisées nulle part côté Java. `v_ration_recommandee` ne pourra de toute façon jamais rien renvoyer tant que Lactation reste non fonctionnel (elle se base sur une lactation active).
- L'affectation ration → vache est entièrement manuelle : aucune vérification que la ration correspond au stade physiologique réel de la vache, ni que le stock suffit pour la durée de l'affectation.

**Alternatives**
- Appeler `AlerteService.envoyerAlerte("stock_aliment_bas", ...)` depuis `MouvementAlimentService`/`AlimentService` quand le stock passe sous le seuil.
- Exposer `v_ration_recommandee` via un service et pré-remplir le formulaire d'affectation avec la ration suggérée (une fois Lactation opérationnel).
- Ajouter un contrôle de cohérence stock/ration à l'affectation (conso quotidienne × durée vs stock disponible).

---

## Alertes

**Manque**
- Seul le module Vaccination alimente réellement les alertes (`VaccinService`) ; les types `stock_lait_bas`, `rappel_velage`, `rappel_chaleur`, `stock_aliment_bas`, `bcs_hors_plage` existent en base mais aucun code ne les génère.
- Pas de création manuelle d'alerte depuis l'UI (seulement via `AlerteApiController`, aucun bouton dans une page).
- Aucune notification (email/push) : l'alerte reste passive tant que personne ne consulte le dashboard.
- Le dashboard Reproduction recalcule lui-même un compteur "alertesActives" par une requête SQL directe (`SELECT COUNT(*) FROM alerte WHERE acquittee = FALSE`) au lieu de réutiliser `AlerteService`/`AlerteRepository`, dupliquant une logique qui existe déjà.

**Alternatives**
- Généraliser le pattern déjà utilisé pour Vaccination (`envoyerAlerte`) aux autres modules au fur et à mesure de leur implémentation (Reproduction, Alimentation, Cheptel).
- Ajouter un petit formulaire "créer une alerte manuelle" pour les cas non couverts par une règle automatique.
- Ajouter un job planifié (`@Scheduled`) qui recalcule les règles (stock, BCS, vêlage à venir...) une fois par jour, plutôt que de dépendre d'une consultation utilisateur pour déclencher le calcul.
- Faire passer tous les compteurs "alertes actives" par `AlerteService` au lieu de requêtes SQL réécrites dans chaque module.

---

## Vente

**Manque**
- Impossible de modifier ou supprimer une vente une fois enregistrée (aucune route edit/delete dans `VenteController`).
- L'utilisateur créateur est codé en dur (`utilisateurRepository.findById(1L)`) au lieu de dépendre de l'utilisateur connecté.
- La vue `v_offre_vente_jour` (écart production vs vente par jour) existe en base mais n'est exploitée nulle part — aucun graphique/rapport ne permet la décision "augmenter/réduire le troupeau".
- Le contrôle de stock avant vente (`productionRepository.getRemainingStock()`) dépend entièrement de la fiabilité du module Production, qui plante actuellement dès qu'on essaie d'enregistrer une production (voir tableau de redondances) — en pratique, `quantite_restante` ne sera donc jamais alimenté normalement.

**Alternatives**
- Ajouter edit/delete de vente (avec recalcul de `quantite_restante` sur les productions concernées).
- Brancher `VenteService` sur l'utilisateur réellement authentifié dès qu'une gestion de session/login existe.
- Construire un graphique "Production vs Vente (7/30 derniers jours)" à partir de `v_offre_vente_jour` pour matérialiser la décision d'ajustement du troupeau.
- Corriger d'abord le bug bloquant de Production (voir ci-dessous) : sans ça, ce module reste fragile par ricochet.

---

## Redondances entre modules (interdépendances)

| Modules concernés | Redondance / problème |
|---|---|
| Reproduction ↔ Cheptel | `vache_statut` (historique de statut) duplique `vache.id_statut` (statut courant) — deux sources de vérité pour le même concept, et la première n'est jamais alimentée par le code. |
| Reproduction ↔ Alertes | Les alertes de vêlage sont calculées deux fois en théorie : une fois via `ReproductionAlerteService` (page dédiée, jamais persistée), une fois prévue nativement dans le module Alertes central (`rappel_velage` en base) mais jamais déclenchée réellement — deux mécanismes concurrents pour la même information. |
| Reproduction ↔ Production/Lactation | Le vêlage devrait déclencher une nouvelle Lactation (donc de la Production), comme décrit dans le cahier des charges (vêlage → lactation → production), mais aucun lien de code n'existe entre ces trois modules. |
| Alimentation ↔ Alertes | Le seuil de stock aliment bas est recalculé "à la main" dans `AlimentController` au lieu d'utiliser `AlerteService`/le type `stock_aliment_bas` déjà prévu — même schéma de redondance que Reproduction ↔ Alertes. |
| Santé ↔ Alertes ↔ Vente | Les rappels de vaccin passent bien par `AlerteService` (bon pattern à généraliser), mais les traitements (`TraitementSante`, délais d'attente lait/viande) n'ont aucun lien avec Alertes ni avec Vente, qui pourrait pourtant bloquer une vente pendant le délai d'attente. |
| Vente ↔ Production | `VenteService` dépend de `Production.quantiteRestante` (stock de lait), mais Production a un bug bloquant (`lactation_id` obligatoire jamais renseigné) — Vente hérite donc de la fragilité de Production dès qu'une vraie production est enregistrée. |
| Cheptel ↔ Alimentation | La recommandation automatique de ration (vue `v_ration_recommandee`) se base sur la lactation active d'une vache — tant que Lactation n'est pas fonctionnel, cette vue ne peut jamais rien renvoyer d'utile, même si elle est correctement écrite en base. |

## Optimisations backend possibles

| Optimisation | Détail |
|---|---|
| Centraliser la génération d'alertes | Un seul point d'entrée (`AlerteService.envoyerAlerte`), déjà utilisé par Vaccination, à réutiliser pour Reproduction (vêlage/chaleur), Alimentation (stock bas) et Cheptel (BCS hors plage) au lieu de dupliquer la logique de comptage/urgence dans chaque service. |
| Trancher sur `vache_statut` | Soit l'alimenter réellement partout où le statut d'une vache change (vêlage, chaleur, mise à la reproduction...), soit la retirer et se reposer uniquement sur `vache.id_statut` + les colonnes déjà présentes sur `reproduction`, pour ne pas garder deux sources de vérité. |
| Introduire un job planifié (`@Scheduled`) | Recalculer une fois par jour les règles d'alerte (stock, vêlage à venir, BCS...) plutôt que de les recalculer à la demande à chaque chargement de page (cas actuel de `getAlertesVelage`, `getChaleurs`, du calcul de stock dans `AlimentController`). |
| Uniformiser le suivi de stock | Production utilise un solde courant (`quantite_restante` décrémenté à chaque vente) alors qu'Alimentation utilise un vrai grand livre de mouvements (`mouvement_aliment` entrée/sortie) — choisir un seul pattern (le grand livre est plus traçable et auditable) pour les deux. |
| Réutiliser les vues SQL déjà écrites | `v_ration_recommandee`, `v_offre_vente_jour`, `v_reproduction_suivi` existent en base mais ne sont jamais requêtées depuis le code Java — les exposer via des services au lieu de réécrire des requêtes JDBC ad hoc similaires (ex : `getSuiviSemaine` recode une partie de ce que `v_reproduction_suivi` fait déjà). |
| Faire du vêlage une vraie transaction métier | `enregistrerVelage` devrait, dans une seule transaction, créer la `Lactation`, mettre à jour `vache.id_statut`, et déclencher une alerte de renouvellement si besoin — actuellement ces trois effets attendus n'existent qu'à moitié (aucun n'est fait en réalité). |
| Corriger le bug bloquant de Production | `Production.lactation` est `nullable = false` mais rien ne le renseigne dans `ProductionController` → tout enregistrement plante avec une violation de contrainte NOT NULL. À corriger avant que quiconque n'utilise ce module en pratique. |
