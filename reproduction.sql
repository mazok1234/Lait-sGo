CREATE OR REPLACE VIEW v_dashboard_suivi_chaleurs AS
WITH dates_ia AS (
    -- 1. On récupère les inséminations réelles passées ou à venir
    SELECT 
        v.id AS vache_id,
        v.numero_boucle,
        r.date_ia AS date_evenement,
        'Insémination Artificielle'::varchar(50) AS type_evenement,
        r.gestation_confirmee
    FROM reproduction r
    JOIN vache v ON v.id = r.vache_id
    WHERE r.date_velage_reel IS NULL -- On ne suit que la reproduction en cours
),
prochaines_chaleurs_theoriques AS (
    -- 2. On calcule le retour en chaleur théorique (J+21) si la gestation n'est pas encore confirmée
    SELECT 
        v.id AS vache_id,
        v.numero_boucle,
        (r.date_ia + INTERVAL '21 days')::date AS date_evenement,
        'Vigilance Retour Chaleurs (J+21)'::varchar(50) AS type_evenement,
        r.gestation_confirmee
    FROM reproduction r
    JOIN vache v ON v.id = r.vache_id
    WHERE r.date_velage_reel IS NULL 
      AND (r.gestation_confirmee IS NULL OR r.gestation_confirmee = FALSE)
)
-- On fusionne le tout pour alimenter le calendrier du Dashboard
SELECT vache_id, numero_boucle, date_evenement, type_evenement FROM dates_ia
UNION ALL
SELECT vache_id, numero_boucle, date_evenement, type_evenement FROM prochaines_chaleurs_theoriques;