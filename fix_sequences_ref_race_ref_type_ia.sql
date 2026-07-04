SELECT setval('ref_race_id_seq', (SELECT MAX(id) FROM ref_race));
SELECT setval('ref_type_ia_id_seq', (SELECT MAX(id) FROM ref_type_ia));
