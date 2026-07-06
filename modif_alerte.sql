DROP TABLE IF EXISTS alerte;

DROP TABLE IF EXISTS ref_type_alerte;

CREATE TABLE alerte (
    id          BIGSERIAL    PRIMARY KEY,
    id_niveau   INT          NOT NULL REFERENCES ref_niveau_alerte(id),
    type_alerte VARCHAR(50)  NOT NULL,
    titre       VARCHAR(200) NOT NULL,
    description TEXT,
    vache_id    BIGINT       REFERENCES vache(id),
    acquittee   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- donnees de test a supprimer apres la mise en production
INSERT INTO alerte (id_niveau, type_alerte, titre, description, vache_id, acquittee)
VALUES
  ((SELECT id FROM ref_niveau_alerte WHERE code = 'urgent'),
   'vaccin_en_retard', 'Vaccin en retard — V001',
   'Vaccin IBR en retard depuis le 2026-06-01.', null, false),

  ((SELECT id FROM ref_niveau_alerte WHERE code = 'attention'),
   'stock_aliment_bas', 'Stock aliment insuffisant — Foin',
   'Stock actuel : 150 kg, seuil : 500 kg.', null, false),

  ((SELECT id FROM ref_niveau_alerte WHERE code = 'info'),
   'rappel_velage', 'Vêlage prévu dans 5 jours',
   'Date prévue : 2026-07-10 (IA du 2026-09-03).', null, false);