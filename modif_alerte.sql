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

