
ALTER TABLE ration
    ADD COLUMN production_min_l DECIMAL(5,1),
    ADD COLUMN production_max_l DECIMAL(5,1),
    ADD COLUMN bcs_min DECIMAL(3,2),
    ADD COLUMN bcs_max DECIMAL(3,2),
    ADD COLUMN id_statut_sante INT REFERENCES ref_statut_sante(id),
    ADD COLUMN priorite INT NOT NULL DEFAULT 0;

-- Les rations spécialisées (production/BCS/santé) n'ont pas de phase de lactation dédiée
ALTER TABLE ration ALTER COLUMN id_phase_lactation DROP NOT NULL;

-- Les rations spécialisées elles-mêmes (Ration haute/basse production, BCS
-- faible/élevé, convalescence malade/traitement) sont dans donnee.sql.

-- 3. Vue de suggestion automatique de ration
CREATE OR REPLACE VIEW v_suggestion_ration AS
WITH production_moyenne_7j AS (
    SELECT
        p.vache_id,
        AVG(p.quantite_litres) AS moyenne_l
    FROM production p
    WHERE p.date_production >= CURRENT_DATE - 7
    GROUP BY p.vache_id
),
statut_sante_actuel AS (
    SELECT
        vhs.vache_id,
        vhs.statut_id,
        rs.libelle AS statut_libelle
    FROM vache_historique_sante vhs
    JOIN ref_statut_sante rs ON rs.id = vhs.statut_id
    WHERE vhs.date_fin IS NULL
),
bcs_actuel AS (
    SELECT
        v.id AS vache_id,
        v.score_bcs,
        v.score_locomotion
    FROM vache v
    WHERE v.score_bcs IS NOT NULL
)
SELECT
    v.id AS vache_id,
    v.numero_boucle,
    v.score_bcs,
    v.score_locomotion,
    l.date_debut AS debut_lactation,
    (CURRENT_DATE - l.date_debut) AS jours_en_lait,
    sp.libelle AS phase_actuelle,
    sp.id AS phase_id,
    pm.moyenne_l AS production_moyenne_7j,
    ss.statut_id AS statut_sante_id,
    ss.statut_libelle AS statut_sante_libelle,
    -- Ration actuelle (via affectation active)
    ar_actuelle.ration_id AS ration_actuelle_id,
    ra_actuelle_nom.nom AS ration_actuelle_nom,
    -- Ration suggérée
    r_sugg.id AS ration_suggeree_id,
    r_sugg.nom AS ration_suggeree_nom,
    r_sugg.priorite AS priorite_suggestion,
    -- Explication de la suggestion
    CASE
        WHEN ss.statut_id IS NOT NULL AND r_sugg.id_statut_sante IS NOT NULL
            THEN 'Vache ' || ss.statut_libelle || ' - Ration de convalescence recommandée'
        WHEN bcs.score_bcs IS NOT NULL AND (
            (bcs.score_bcs < 2.5 AND r_sugg.bcs_max IS NOT NULL)
            OR (bcs.score_bcs > 4.0 AND r_sugg.bcs_min IS NOT NULL)
        )
            THEN 'BCS ' || bcs.score_bcs || ' anormal - Ration adaptée recommandée'
        WHEN pm.moyenne_l IS NOT NULL AND (
            (pm.moyenne_l > 25.0 AND r_sugg.production_min_l IS NOT NULL)
            OR (pm.moyenne_l < 15.0 AND r_sugg.production_max_l IS NOT NULL)
        )
            THEN 'Production moyenne ' || ROUND(pm.moyenne_l::numeric, 1) || 'L/j - Ration adaptée recommandée'
        ELSE 'Ration standard par phase de lactation'
    END AS raison_suggestion
FROM vache v
JOIN lactation l ON l.vache_id = v.id
    AND l.id_statut = (SELECT id FROM ref_statut_lactation WHERE code = 'active')
JOIN ref_phase_lactation sp
    ON (CURRENT_DATE - l.date_debut) BETWEEN sp.jour_min AND sp.jour_max
LEFT JOIN production_moyenne_7j pm ON pm.vache_id = v.id
LEFT JOIN bcs_actuel bcs ON bcs.vache_id = v.id
LEFT JOIN statut_sante_actuel ss ON ss.vache_id = v.id
-- Ration actuelle via affectation active
LEFT JOIN affectation_ration_vache ar_actuelle
    ON ar_actuelle.vache_id = v.id AND ar_actuelle.actif = TRUE
LEFT JOIN ration ra_actuelle_nom ON ra_actuelle_nom.id = ar_actuelle.ration_id
-- Ration suggérée : on prend la meilleure correspondance par priorité
LEFT JOIN LATERAL (
    SELECT r.id, r.nom, r.priorite, r.id_statut_sante,
           r.production_min_l, r.production_max_l,
           r.bcs_min, r.bcs_max
    FROM ration r
    WHERE (
        -- Ration standard par phase (priorite = 0)
        (r.priorite = 0 AND r.id_phase_lactation = sp.id)
        -- OU ration spécialisée par production
        OR (r.priorite = 1 AND r.id_phase_lactation IS NULL
            AND pm.moyenne_l IS NOT NULL
            AND (r.production_min_l IS NULL OR pm.moyenne_l >= r.production_min_l)
            AND (r.production_max_l IS NULL OR pm.moyenne_l <= r.production_max_l))
        -- OU ration spécialisée par BCS
        OR (r.priorite = 2 AND r.id_phase_lactation IS NULL
            AND bcs.score_bcs IS NOT NULL
            AND (r.bcs_min IS NULL OR bcs.score_bcs >= r.bcs_min)
            AND (r.bcs_max IS NULL OR bcs.score_bcs <= r.bcs_max))
        -- OU ration spécialisée par santé
        OR (r.priorite = 3 AND r.id_phase_lactation IS NULL
            AND ss.statut_id IS NOT NULL
            AND r.id_statut_sante = ss.statut_id)
    )
    ORDER BY r.priorite DESC
    LIMIT 1
) r_sugg ON TRUE
WHERE v.id IS NOT NULL
ORDER BY r_sugg.priorite DESC NULLS LAST, v.numero_boucle;