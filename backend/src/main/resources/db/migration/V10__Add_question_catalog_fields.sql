-- Add catalog-related fields to questions table
ALTER TABLE questions ADD COLUMN IF NOT EXISTS catalog_category VARCHAR(50) DEFAULT 'GENERAL';
ALTER TABLE questions ADD COLUMN IF NOT EXISTS approval_status VARCHAR(50) DEFAULT 'PENDING_REVIEW';
ALTER TABLE questions ADD COLUMN IF NOT EXISTS approval_notes TEXT;
ALTER TABLE questions ADD COLUMN IF NOT EXISTS approved_by VARCHAR(255);
ALTER TABLE questions ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP;

-- Add check constraint for approval_status if not already present
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_name = 'questions' AND constraint_name = 'questions_approval_status_check'
  ) THEN
    ALTER TABLE questions ADD CONSTRAINT questions_approval_status_check
      CHECK (approval_status IN ('PENDING_REVIEW', 'APPROVED', 'REJECTED'));
  END IF;
END $$;

-- Add check constraint for catalog_category if not already present
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_name = 'questions' AND constraint_name = 'questions_catalog_category_check'
  ) THEN
    ALTER TABLE questions ADD CONSTRAINT questions_catalog_category_check
      CHECK (catalog_category IN ('GENERAL', 'BEGINNER', 'INTERMEDIATE', 'ADVANCED'));
  END IF;
END $$;
