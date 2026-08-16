-- Fix cefrLevel column size from varchar(5) to varchar(20)
-- This allows storing values like "INTERMEDIATE" (12 chars) instead of just "B1" (2 chars)

ALTER TABLE questions
  ALTER COLUMN cefr_level TYPE varchar(20);

-- Verify the change
-- SELECT column_name, data_type, character_maximum_length
-- FROM information_schema.columns
-- WHERE table_name = 'questions' AND column_name = 'cefr_level';
