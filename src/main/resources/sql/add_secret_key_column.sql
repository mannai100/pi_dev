-- Ajouter la colonne secret_key à la table user si elle n'existe pas déjà
ALTER TABLE user ADD COLUMN IF NOT EXISTS secret_key VARCHAR(255) DEFAULT NULL;
