-- Add or update check constraints for all status columns
-- Ensures database constraints match Java enum definitions

-- Assessment status constraint
DO $$ BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name='assessments' AND column_name='status'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE table_name='assessments' AND constraint_name='assessments_status_check'
    ) THEN
      ALTER TABLE assessments
        ADD CONSTRAINT assessments_status_check CHECK (
          status IN ('DRAFT', 'ACTIVE', 'CLOSED', 'ARCHIVED')
        );
    END IF;
  END IF;
END $$;

-- Submission status constraint
DO $$ BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name='assessment_submissions' AND column_name='status'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE table_name='assessment_submissions' AND constraint_name='submissions_status_check'
    ) THEN
      ALTER TABLE assessment_submissions
        ADD CONSTRAINT submissions_status_check CHECK (
          status IN ('IN_PROGRESS', 'SUBMITTED', 'EVALUATING', 'EVALUATED')
        );
    END IF;
  END IF;
END $$;

-- Candidate status constraint
DO $$ BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name='assessment_candidates' AND column_name='status'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE table_name='assessment_candidates' AND constraint_name='candidates_status_check'
    ) THEN
      ALTER TABLE assessment_candidates
        ADD CONSTRAINT candidates_status_check CHECK (
          status IN ('INVITED', 'STARTED', 'COMPLETED', 'EVALUATED')
        );
    END IF;
  END IF;
END $$;

-- User role constraint
DO $$ BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name='users' AND column_name='role'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE table_name='users' AND constraint_name='users_role_check'
    ) THEN
      ALTER TABLE users
        ADD CONSTRAINT users_role_check CHECK (
          role IN ('ROLE_ADMIN', 'ROLE_CANDIDATE')
        );
    END IF;
  END IF;
END $$;
