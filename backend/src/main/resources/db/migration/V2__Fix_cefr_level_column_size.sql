-- Fix cefrLevel column size from varchar(5) to varchar(20)
-- This allows storing CEFR level codes: A1, A2, B1, B2, C1, C2

ALTER TABLE questions
  ALTER COLUMN cefr_level TYPE varchar(20);

-- Verify the change
-- SELECT column_name, data_type, character_maximum_length
-- FROM information_schema.columns
-- WHERE table_name = 'questions' AND column_name = 'cefr_level';
