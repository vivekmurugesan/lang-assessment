-- Add content generation status tracking to questions table
ALTER TABLE questions ADD COLUMN IF NOT EXISTS audio_generation_status VARCHAR(50) NOT NULL DEFAULT 'NOT_REQUIRED';
ALTER TABLE questions ADD COLUMN IF NOT EXISTS options_generation_status VARCHAR(50) NOT NULL DEFAULT 'NOT_REQUIRED';
ALTER TABLE questions ADD COLUMN IF NOT EXISTS audio_generation_error TEXT;
ALTER TABLE questions ADD COLUMN IF NOT EXISTS options_generation_error TEXT;

-- Add check constraint for valid status values
ALTER TABLE questions ADD CONSTRAINT check_audio_generation_status
  CHECK (audio_generation_status IN ('NOT_REQUIRED', 'PENDING', 'GENERATING', 'GENERATED', 'FAILED'));

ALTER TABLE questions ADD CONSTRAINT check_options_generation_status
  CHECK (options_generation_status IN ('NOT_REQUIRED', 'PENDING', 'GENERATING', 'GENERATED', 'FAILED'));
