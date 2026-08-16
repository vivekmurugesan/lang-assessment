-- Fix questions table status check constraint
-- The existing constraint doesn't allow PENDING_REVIEW which is a valid QuestionStatus enum value

-- Drop the old check constraint if it exists
DO $$ BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_name='questions' AND constraint_name='questions_status_check'
  ) THEN
    ALTER TABLE questions DROP CONSTRAINT questions_status_check;
  END IF;
END $$;

-- Add new check constraint that allows all valid QuestionStatus values
ALTER TABLE questions
  ADD CONSTRAINT questions_status_check CHECK (
    status IN ('ACTIVE', 'INACTIVE', 'UNDER_REVIEW', 'PENDING_REVIEW', 'REJECTED')
  );
