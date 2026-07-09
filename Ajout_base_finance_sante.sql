-- Scripts d’ajout / migration
-- Ajoute colonne prix_unitaire dans medicament
-- Ajoute colonne nbr_medicament dans traitement_sante

ALTER TABLE medicament
    ADD COLUMN IF NOT EXISTS prix_unitaire DECIMAL(10,2) DEFAULT 0;

ALTER TABLE traitement_sante
    ADD COLUMN IF NOT EXISTS nbr_medicament INT NOT NULL DEFAULT 1;

-- Les données (médicaments, maladies, vaches, historique santé,
-- événements santé, traitements) sont dans donnee.sql.
