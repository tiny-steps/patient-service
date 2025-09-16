-- Add missing status column to patients table
-- This column is required by the Patient entity which includes status field
-- Create the entity_status enum type if it doesn't exist
DO $$ BEGIN IF NOT EXISTS (
    SELECT 1
    FROM pg_type
    WHERE typname = 'entity_status'
) THEN CREATE TYPE entity_status AS ENUM ('ACTIVE', 'INACTIVE', 'DELETED');
END IF;
END $$;
-- Add the status column with default value
ALTER TABLE patients
ADD COLUMN if not exists status entity_status NOT NULL DEFAULT 'ACTIVE';
