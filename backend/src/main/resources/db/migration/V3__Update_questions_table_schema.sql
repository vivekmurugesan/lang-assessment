-- Update questions table schema for MinIO storage and increased field sizes
-- This migration handles all schema changes for the content safety and MinIO refactoring

-- 1. Fix cefrLevel column size from varchar(5) to varchar(20) if it exists
DO $$ BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name='questions' AND column_name='cefr_level'
  ) THEN
    ALTER TABLE questions ALTER COLUMN cefr_level TYPE varchar(20);
  END IF;
END $$;

-- 2. Increase correctAnswer column size from varchar(50) to varchar(100) if it exists
DO $$ BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name='questions' AND column_name='correct_answer'
  ) THEN
    ALTER TABLE questions ALTER COLUMN correct_answer TYPE varchar(100);
  END IF;
END $$;

-- 3. Add or update questionOptionsUri column if it doesn't exist
-- This column stores MinIO URL reference for question options
ALTER TABLE questions
  ADD COLUMN IF NOT EXISTS question_options_uri TEXT;

-- 4. Add or update explanationUri column if it doesn't exist
-- This column stores MinIO URL reference for explanations
ALTER TABLE questions
  ADD COLUMN IF NOT EXISTS explanation_uri TEXT;

-- The questions table now supports:
-- - cefr_level: varchar(20) for full CEFR level names (e.g., "INTERMEDIATE")
-- - correct_answer: varchar(100) for longer answer texts (if column exists)
-- - question_options_uri: TEXT storing MinIO URL for options
-- - explanation_uri: TEXT storing MinIO URL for explanations
