-- Fix difficulty_level column size in assessment_modules table
-- The column was defined as varchar(10) but needs to support "INTERMEDIATE" (12 chars)

-- Safely alter difficulty_level column if it exists
DO $$ BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name='assessment_modules' AND column_name='difficulty_level'
  ) THEN
    ALTER TABLE assessment_modules ALTER COLUMN difficulty_level TYPE varchar(50);
  END IF;
END $$;
