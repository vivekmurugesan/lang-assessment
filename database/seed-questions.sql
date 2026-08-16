-- Seed questions for testing
-- Run this after creating the assessment with modules

-- Get the language ID for English (assuming ID = 1)
-- Get the assessment ID (should be 2 for "EnglishTest")

-- LISTENING questions
INSERT INTO question (language_id, module_type, cefr_level, question_text, question_number, status, created_at, updated_at)
VALUES
  (1, 'LISTENING', 'INTERMEDIATE', 'What is the main topic of the audio?', 1, 'ACTIVE', NOW(), NOW()),
  (1, 'LISTENING', 'INTERMEDIATE', 'According to the speaker, what is the first step?', 2, 'ACTIVE', NOW(), NOW()),
  (1, 'LISTENING', 'INTERMEDIATE', 'When did the event take place?', 3, 'ACTIVE', NOW(), NOW()),
  (1, 'LISTENING', 'INTERMEDIATE', 'What does the speaker recommend?', 4, 'ACTIVE', NOW(), NOW()),
  (1, 'LISTENING', 'INTERMEDIATE', 'Why does the speaker mention this example?', 5, 'ACTIVE', NOW(), NOW()),
  (1, 'LISTENING', 'INTERMEDIATE', 'What is the tone of the speaker?', 6, 'ACTIVE', NOW(), NOW()),
  (1, 'LISTENING', 'INTERMEDIATE', 'How many people attended the meeting?', 7, 'ACTIVE', NOW(), NOW()),
  (1, 'LISTENING', 'INTERMEDIATE', 'What does the speaker conclude?', 8, 'ACTIVE', NOW(), NOW()),
  (1, 'LISTENING', 'INTERMEDIATE', 'Which option best summarizes the content?', 9, 'ACTIVE', NOW(), NOW()),
  (1, 'LISTENING', 'INTERMEDIATE', 'Where is the conversation taking place?', 10, 'ACTIVE', NOW(), NOW());

-- READING questions
INSERT INTO question (language_id, module_type, cefr_level, question_text, question_number, status, created_at, updated_at)
VALUES
  (1, 'READING', 'INTERMEDIATE', 'What is the main idea of the passage?', 1, 'ACTIVE', NOW(), NOW()),
  (1, 'READING', 'INTERMEDIATE', 'According to the text, what happened first?', 2, 'ACTIVE', NOW(), NOW()),
  (1, 'READING', 'INTERMEDIATE', 'What does "this" refer to in paragraph 2?', 3, 'ACTIVE', NOW(), NOW()),
  (1, 'READING', 'INTERMEDIATE', 'How does the author feel about the situation?', 4, 'ACTIVE', NOW(), NOW()),
  (1, 'READING', 'INTERMEDIATE', 'Which of the following is mentioned as a benefit?', 5, 'ACTIVE', NOW(), NOW()),
  (1, 'READING', 'INTERMEDIATE', 'What can be inferred from the last paragraph?', 6, 'ACTIVE', NOW(), NOW()),
  (1, 'READING', 'INTERMEDIATE', 'The passage is primarily concerned with...', 7, 'ACTIVE', NOW(), NOW()),
  (1, 'READING', 'INTERMEDIATE', 'Based on the text, the author would likely agree that...', 8, 'ACTIVE', NOW(), NOW()),
  (1, 'READING', 'INTERMEDIATE', 'What is the purpose of the example in paragraph 3?', 9, 'ACTIVE', NOW(), NOW()),
  (1, 'READING', 'INTERMEDIATE', 'The passage suggests that future developments will...', 10, 'ACTIVE', NOW(), NOW());

-- SPOKEN_INTERACTION questions
INSERT INTO question (language_id, module_type, cefr_level, question_text, question_number, status, created_at, updated_at)
VALUES
  (1, 'SPOKEN_INTERACTION', 'INTERMEDIATE', 'Introduce yourself and explain why you are interested in this course.', 1, 'ACTIVE', NOW(), NOW()),
  (1, 'SPOKEN_INTERACTION', 'INTERMEDIATE', 'Describe your most memorable travel experience.', 2, 'ACTIVE', NOW(), NOW()),
  (1, 'SPOKEN_INTERACTION', 'INTERMEDIATE', 'What are the advantages and disadvantages of working from home?', 3, 'ACTIVE', NOW(), NOW()),
  (1, 'SPOKEN_INTERACTION', 'INTERMEDIATE', 'How has technology changed your daily life?', 4, 'ACTIVE', NOW(), NOW()),
  (1, 'SPOKEN_INTERACTION', 'INTERMEDIATE', 'Describe a problem you recently solved and how you solved it.', 5, 'ACTIVE', NOW(), NOW()),
  (1, 'SPOKEN_INTERACTION', 'INTERMEDIATE', 'What do you think are the most important skills for the future?', 6, 'ACTIVE', NOW(), NOW()),
  (1, 'SPOKEN_INTERACTION', 'INTERMEDIATE', 'Tell us about your hobbies and why you enjoy them.', 7, 'ACTIVE', NOW(), NOW()),
  (1, 'SPOKEN_INTERACTION', 'INTERMEDIATE', 'How do you prefer to learn new information?', 8, 'ACTIVE', NOW(), NOW()),
  (1, 'SPOKEN_INTERACTION', 'INTERMEDIATE', 'Discuss the importance of education in society.', 9, 'ACTIVE', NOW(), NOW()),
  (1, 'SPOKEN_INTERACTION', 'INTERMEDIATE', 'What are your career goals for the next five years?', 10, 'ACTIVE', NOW(), NOW());

-- SPOKEN_PRODUCTION questions
INSERT INTO question (language_id, module_type, cefr_level, question_text, question_number, status, created_at, updated_at)
VALUES
  (1, 'SPOKEN_PRODUCTION', 'INTERMEDIATE', 'Read the following text aloud clearly and fluently.', 1, 'ACTIVE', NOW(), NOW()),
  (1, 'SPOKEN_PRODUCTION', 'INTERMEDIATE', 'Summarize the main points of the article you just read.', 2, 'ACTIVE', NOW(), NOW()),
  (1, 'SPOKEN_PRODUCTION', 'INTERMEDIATE', 'Explain the process of how renewable energy works.', 3, 'ACTIVE', NOW(), NOW()),
  (1, 'SPOKEN_PRODUCTION', 'INTERMEDIATE', 'Describe your ideal weekend and explain why.', 4, 'ACTIVE', NOW(), NOW()),
  (1, 'SPOKEN_PRODUCTION', 'INTERMEDIATE', 'Give your opinion on climate change and provide reasons.', 5, 'ACTIVE', NOW(), NOW());

-- WRITING questions
INSERT INTO question (language_id, module_type, cefr_level, question_text, question_number, status, created_at, updated_at)
VALUES
  (1, 'WRITING', 'INTERMEDIATE', 'Write an email to a friend about your recent vacation. (Minimum 150 words)', 1, 'ACTIVE', NOW(), NOW()),
  (1, 'WRITING', 'INTERMEDIATE', 'Write a formal letter of complaint to a company about a faulty product. (Minimum 150 words)', 2, 'ACTIVE', NOW(), NOW()),
  (1, 'WRITING', 'INTERMEDIATE', 'Write an essay on the benefits of learning a second language. (Minimum 200 words)', 3, 'ACTIVE', NOW(), NOW()),
  (1, 'WRITING', 'INTERMEDIATE', 'Describe your favorite place and why it is special to you. (Minimum 150 words)', 4, 'ACTIVE', NOW(), NOW()),
  (1, 'WRITING', 'INTERMEDIATE', 'Write a review of a book, movie, or restaurant you recently experienced. (Minimum 150 words)', 5, 'ACTIVE', NOW(), NOW());

-- Verify the inserts
SELECT COUNT(*) as total_questions, module_type FROM question GROUP BY module_type;
