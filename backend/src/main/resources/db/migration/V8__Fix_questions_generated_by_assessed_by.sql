-- Fix generated_by and assessed_by column sizes in questions table
-- These columns store email addresses which can exceed 50 characters

-- Safely alter generated_by column if it exists
DO $$ BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name='questions' AND column_name='generated_by'
  ) THEN
    ALTER TABLE questions ALTER COLUMN generated_by TYPE varchar(255);
  END IF;
END $$;

-- Safely alter assessed_by column if it exists
DO $$ BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name='questions' AND column_name='assessed_by'
  ) THEN
    ALTER TABLE questions ALTER COLUMN assessed_by TYPE varchar(255);
  END IF;
END $$;
