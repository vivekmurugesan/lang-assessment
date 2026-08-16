-- Fix cefr_level column size in assessment_submissions table
-- The column was defined as varchar(5) but needs to support CEFR codes (A1, A2, B1, B2, C1, C2)

-- Safely alter cefr_level column if it exists
DO $$ BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name='assessment_submissions' AND column_name='cefr_level'
  ) THEN
    ALTER TABLE assessment_submissions ALTER COLUMN cefr_level TYPE varchar(20);
  END IF;
END $$;
