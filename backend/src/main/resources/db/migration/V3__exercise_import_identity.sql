ALTER TABLE exercises ADD CONSTRAINT exercises_external_identity_unique UNIQUE (external_source, external_id);
