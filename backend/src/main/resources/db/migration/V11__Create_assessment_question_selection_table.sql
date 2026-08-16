-- Create table for tracking which questions are selected for each assessment
CREATE TABLE IF NOT EXISTS assessment_question_selection (
    id SERIAL PRIMARY KEY,
    assessment_id INTEGER NOT NULL,
    question_id INTEGER NOT NULL,
    sequence_number INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assessment_id) REFERENCES assessments(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    UNIQUE(assessment_id, question_id)
);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_assessment_question_selection_assessment_id
  ON assessment_question_selection(assessment_id);
CREATE INDEX IF NOT EXISTS idx_assessment_question_selection_question_id
  ON assessment_question_selection(question_id);
