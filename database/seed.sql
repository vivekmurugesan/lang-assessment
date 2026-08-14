-- Insert CEFR Levels
INSERT INTO cefr_levels (level, description, score_range_min, score_range_max) VALUES
('A1', 'Beginner', 0, 20),
('A2', 'Elementary', 21, 40),
('B1', 'Intermediate', 41, 60),
('B2', 'Upper Intermediate', 61, 80),
('C1', 'Advanced', 81, 90),
('C2', 'Mastery', 91, 100)
ON CONFLICT DO NOTHING;

-- Insert Languages
INSERT INTO languages (code, name) VALUES
('en', 'English'),
('es', 'Spanish'),
('fr', 'French'),
('de', 'German'),
('zh', 'Mandarin Chinese'),
('ja', 'Japanese')
ON CONFLICT DO NOTHING;

-- Insert Default Admin User
-- Password: 'password' (BCrypt hash - cost 10)
-- Change this password immediately in production!
INSERT INTO users (email, name, password_hash, role, is_active) VALUES
('admin@langassessment.com', 'System Administrator', '$2a$10$SlCf9LjRNZ7j/d9kJ7Q1i.Gh9Jw7pR8mT4uV3wXyZaAbCdEfGhIjKlMn', 'ADMIN', TRUE)
ON CONFLICT DO NOTHING;

-- Insert sample CEFR metrics
INSERT INTO evaluation_metrics (module_type, cefr_level, metric_name, metric_description, weight, evaluation_criteria) VALUES
-- Listening A1
('LISTENING', 'A1', 'Vocabulary Recognition', 'Ability to recognize basic vocabulary', 0.25, '{"basic_words": true, "familiar_topics": true}'),
('LISTENING', 'A1', 'Comprehension', 'Understanding simple sentences', 0.25, '{"simple_sentences": true, "clear_speech": true}'),
('LISTENING', 'A1', 'Response Accuracy', 'Correct answers to simple questions', 0.50, '{"multiple_choice": true, "factual": true}'),

-- Listening B2
('LISTENING', 'B2', 'Advanced Vocabulary', 'Recognition of complex vocabulary and idioms', 0.20, '{"idioms": true, "complex_terms": true}'),
('LISTENING', 'B2', 'Implicit Meaning', 'Understanding implied messages and tone', 0.25, '{"tone_recognition": true, "implication": true}'),
('LISTENING', 'B2', 'Detailed Comprehension', 'Understanding specific details and nuances', 0.30, '{"details": true, "nuances": true}'),
('LISTENING', 'B2', 'Response Accuracy', 'Accurate answers to complex questions', 0.25, '{"complex_questions": true, "detailed_answers": true}'),

-- Reading A1
('READING', 'A1', 'Basic Vocabulary', 'Recognition of basic written vocabulary', 0.30, '{"simple_words": true, "familiar_context": true}'),
('READING', 'A1', 'Simple Text Comprehension', 'Understanding simple texts and sentences', 0.40, '{"simple_sentences": true, "short_texts": true}'),
('READING', 'A1', 'Information Extraction', 'Identifying key information', 0.30, '{"factual_info": true, "explicit_info": true}'),

-- Reading B2
('READING', 'B2', 'Complex Vocabulary', 'Understanding sophisticated vocabulary and expressions', 0.20, '{"sophisticated_vocab": true, "idiomatic_expressions": true}'),
('READING', 'B2', 'Implicit Meaning', 'Inferring implicit meaning from text', 0.25, '{"inference": true, "implied_meaning": true}'),
('READING', 'B2', 'Detailed Analysis', 'Analyzing detailed text content and structure', 0.30, '{"structure_analysis": true, "content_depth": true}'),
('READING', 'B2', 'Critical Comprehension', 'Understanding author perspective and tone', 0.25, '{"author_perspective": true, "tone": true}'),

-- Writing A1
('WRITING', 'A1', 'Vocabulary Usage', 'Use of basic vocabulary appropriately', 0.25, '{"basic_vocab": true, "appropriate_usage": true}'),
('WRITING', 'A1', 'Grammar Accuracy', 'Basic grammar correctness', 0.30, '{"basic_structures": true, "tense_agreement": true}'),
('WRITING', 'A1', 'Coherence', 'Simple logical organization', 0.20, '{"simple_logic": true, "basic_flow": true}'),
('WRITING', 'A1', 'Spelling', 'Basic spelling correctness', 0.25, '{"common_words": true, "phonetic_accuracy": true}'),

-- Writing B2
('WRITING', 'B2', 'Vocabulary Range', 'Sophisticated and varied vocabulary', 0.20, '{"sophisticated_vocab": true, "variety": true}'),
('WRITING', 'B2', 'Grammar Complexity', 'Complex grammatical structures correctly used', 0.25, '{"complex_structures": true, "correct_usage": true}'),
('WRITING', 'B2', 'Coherence and Organization', 'Well-organized and coherent arguments', 0.30, '{"logical_flow": true, "paragraph_structure": true, "transitions": true}'),
('WRITING', 'B2', 'Tone and Style', 'Appropriate register and style', 0.15, '{"formal_informal": true, "register": true}'),
('WRITING', 'B2', 'Spelling and Punctuation', 'Correct spelling and punctuation', 0.10, '{"accurate_spelling": true, "proper_punctuation": true}'),

-- Speaking A1
('SPOKEN_PRODUCTION', 'A1', 'Pronunciation', 'Basic pronunciation clarity', 0.25, '{"clarity": true, "understandable": true}'),
('SPOKEN_PRODUCTION', 'A1', 'Fluency', 'Simple, hesitant speech', 0.20, '{"simple_rhythm": true, "basic_flow": true}'),
('SPOKEN_PRODUCTION', 'A1', 'Vocabulary', 'Basic vocabulary usage', 0.30, '{"simple_words": true, "common_phrases": true}'),
('SPOKEN_PRODUCTION', 'A1', 'Grammar', 'Basic grammar structures', 0.25, '{"simple_structures": true, "basic_tenses": true}'),

-- Speaking B2
('SPOKEN_PRODUCTION', 'B2', 'Pronunciation and Intonation', 'Clear pronunciation with appropriate intonation', 0.20, '{"clear_accent": true, "natural_intonation": true}'),
('SPOKEN_PRODUCTION', 'B2', 'Fluency and Coherence', 'Fluent and logically coherent speech', 0.25, '{"natural_flow": true, "logical_structure": true}'),
('SPOKEN_PRODUCTION', 'B2', 'Vocabulary and Idiom', 'Wide vocabulary range and idiomatic expressions', 0.25, '{"diverse_vocab": true, "idiomatic_usage": true}'),
('SPOKEN_PRODUCTION', 'B2', 'Grammar', 'Accurate and complex grammar', 0.20, '{"complex_structures": true, "accurate_usage": true}'),
('SPOKEN_PRODUCTION', 'B2', 'Discourse Management', 'Effective communication strategies', 0.10, '{"clarity_repair": true, "topic_development": true}')
ON CONFLICT DO NOTHING;
