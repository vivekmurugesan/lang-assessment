-- Fix correct_answer column size in questions table
-- Change from varchar(100) to TEXT to support longer answers (essays, detailed responses)

-- Safely alter correct_answer column if it exists
DO $$ BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name='questions' AND column_name='correct_answer'
  ) THEN
    ALTER TABLE questions ALTER COLUMN correct_answer TYPE TEXT;
  END IF;
END $$;
