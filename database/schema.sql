-- Users table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'CANDIDATE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- Languages table
CREATE TABLE IF NOT EXISTS languages (
    id SERIAL PRIMARY KEY,
    code VARCHAR(10) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CEFR Levels table
CREATE TABLE IF NOT EXISTS cefr_levels (
    id SERIAL PRIMARY KEY,
    level VARCHAR(5) NOT NULL UNIQUE CHECK (level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    description TEXT,
    score_range_min INT,
    score_range_max INT
);

-- Assessments table
CREATE TABLE IF NOT EXISTS assessments (
    id SERIAL PRIMARY KEY,
    admin_id INT NOT NULL REFERENCES users(id),
    language_id INT NOT NULL REFERENCES languages(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL CHECK (status IN ('DRAFT', 'ACTIVE', 'CLOSED', 'ARCHIVED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP
);

CREATE INDEX idx_assessments_admin_id ON assessments(admin_id);
CREATE INDEX idx_assessments_language_id ON assessments(language_id);
CREATE INDEX idx_assessments_status ON assessments(status);

-- Assessment Modules Configuration
CREATE TABLE IF NOT EXISTS assessment_modules (
    id SERIAL PRIMARY KEY,
    assessment_id INT NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    module_type VARCHAR(50) NOT NULL CHECK (module_type IN ('LISTENING', 'READING', 'SPOKEN_INTERACTION', 'SPOKEN_PRODUCTION', 'WRITING')),
    num_questions INT NOT NULL,
    difficulty_level VARCHAR(10),
    is_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_assessment_modules_assessment_id ON assessment_modules(assessment_id);

-- Candidates table (Assessment specific)
CREATE TABLE IF NOT EXISTS assessment_candidates (
    id SERIAL PRIMARY KEY,
    assessment_id INT NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES users(id),
    secure_link VARCHAR(255) UNIQUE NOT NULL,
    temp_password VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('INVITED', 'STARTED', 'COMPLETED', 'EVALUATED')),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_assessment_candidates_assessment_id ON assessment_candidates(assessment_id);
CREATE INDEX idx_assessment_candidates_user_id ON assessment_candidates(user_id);
CREATE INDEX idx_assessment_candidates_secure_link ON assessment_candidates(secure_link);

-- Questions table
CREATE TABLE IF NOT EXISTS questions (
    id SERIAL PRIMARY KEY,
    language_id INT NOT NULL REFERENCES languages(id),
    module_type VARCHAR(50) NOT NULL CHECK (module_type IN ('LISTENING', 'READING', 'SPOKEN_INTERACTION', 'SPOKEN_PRODUCTION', 'WRITING')),
    cefr_level VARCHAR(5) NOT NULL REFERENCES cefr_levels(level),
    question_text TEXT NOT NULL,
    question_number INT,
    status VARCHAR(50) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'UNDER_REVIEW')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_questions_language_id ON questions(language_id);
CREATE INDEX idx_questions_module_type ON questions(module_type);
CREATE INDEX idx_questions_cefr_level ON questions(cefr_level);

-- Question Content (Audio/Text/Images)
CREATE TABLE IF NOT EXISTS question_content (
    id SERIAL PRIMARY KEY,
    question_id INT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    content_type VARCHAR(50) NOT NULL CHECK (content_type IN ('TEXT', 'AUDIO', 'IMAGE')),
    content_path VARCHAR(500) NOT NULL,
    content_size INT,
    mime_type VARCHAR(50),
    duration_seconds INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_question_content_question_id ON question_content(question_id);

-- Answer Options (for multiple choice questions)
CREATE TABLE IF NOT EXISTS answer_options (
    id SERIAL PRIMARY KEY,
    question_id INT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    option_text TEXT,
    option_label VARCHAR(10),
    is_correct BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_answer_options_question_id ON answer_options(question_id);

-- Candidate Submissions
CREATE TABLE IF NOT EXISTS submissions (
    id SERIAL PRIMARY KEY,
    assessment_candidate_id INT NOT NULL REFERENCES assessment_candidates(id) ON DELETE CASCADE,
    question_id INT NOT NULL REFERENCES questions(id),
    answer_text TEXT,
    answer_audio_path VARCHAR(500),
    submission_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_submitted BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_submissions_assessment_candidate_id ON submissions(assessment_candidate_id);
CREATE INDEX idx_submissions_question_id ON submissions(question_id);

-- Evaluation Results
CREATE TABLE IF NOT EXISTS evaluation_results (
    id SERIAL PRIMARY KEY,
    submission_id INT NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    assessment_candidate_id INT NOT NULL REFERENCES assessment_candidates(id),
    module_type VARCHAR(50) NOT NULL,
    score DECIMAL(5, 2),
    max_score DECIMAL(5, 2) DEFAULT 100,
    cefr_level VARCHAR(5),
    metrics JSONB,
    reasoning TEXT,
    evaluated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    evaluator_type VARCHAR(50) CHECK (evaluator_type IN ('AI', 'MANUAL', 'HYBRID'))
);

CREATE INDEX idx_evaluation_results_assessment_candidate_id ON evaluation_results(assessment_candidate_id);
CREATE INDEX idx_evaluation_results_module_type ON evaluation_results(module_type);

-- Assessment Scorecards
CREATE TABLE IF NOT EXISTS scorecards (
    id SERIAL PRIMARY KEY,
    assessment_candidate_id INT NOT NULL UNIQUE REFERENCES assessment_candidates(id),
    listening_score DECIMAL(5, 2),
    listening_level VARCHAR(5),
    reading_score DECIMAL(5, 2),
    reading_level VARCHAR(5),
    spoken_interaction_score DECIMAL(5, 2),
    spoken_interaction_level VARCHAR(5),
    spoken_production_score DECIMAL(5, 2),
    spoken_production_level VARCHAR(5),
    writing_score DECIMAL(5, 2),
    writing_level VARCHAR(5),
    overall_score DECIMAL(5, 2),
    overall_level VARCHAR(5),
    evaluation_completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_scorecards_assessment_candidate_id ON scorecards(assessment_candidate_id);

-- Evaluation Metrics Configuration
CREATE TABLE IF NOT EXISTS evaluation_metrics (
    id SERIAL PRIMARY KEY,
    module_type VARCHAR(50) NOT NULL,
    cefr_level VARCHAR(5) NOT NULL,
    metric_name VARCHAR(255) NOT NULL,
    metric_description TEXT,
    weight DECIMAL(5, 2),
    evaluation_criteria JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(module_type, cefr_level, metric_name)
);

-- API Usage Tracking (for billing)
CREATE TABLE IF NOT EXISTS api_usage_log (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    endpoint VARCHAR(255),
    method VARCHAR(10),
    ai_model_used VARCHAR(100),
    tokens_used INT,
    cost DECIMAL(10, 6),
    status_code INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_api_usage_log_user_id ON api_usage_log(user_id);
CREATE INDEX idx_api_usage_log_created_at ON api_usage_log(created_at);

-- Audit Log
CREATE TABLE IF NOT EXISTS audit_log (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    action VARCHAR(255),
    entity_type VARCHAR(100),
    entity_id INT,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);
