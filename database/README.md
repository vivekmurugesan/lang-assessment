# Database Setup

## Tables Overview

- `user` - Admin and candidate users
- `language` - Languages (English, Spanish, etc.)
- `assessment` - Assessment configurations
- `assessment_module` - Modules within assessments (Listening, Reading, etc.)
- `question` - Question bank for all modules
- `assessment_candidate` - Links candidates to assessments
- `assessment_submission` - Candidate submission records
- `question_response` - Individual responses to questions

## Question Bank

The system relies on a pre-populated `question` table. Questions are organized by:
- **Language** (e.g., English, Spanish)
- **Module Type** (LISTENING, READING, WRITING, SPOKEN_INTERACTION, SPOKEN_PRODUCTION)
- **CEFR Level** (A1, A2, B1, B2, C1, C2)
- **Status** (ACTIVE, INACTIVE, ARCHIVED)

## Seeding Questions

### Quick Start

```bash
# Using Docker
docker-compose exec postgres psql -U postgres -d lang_assessment -f /database/seed-questions.sql
```

### Manual Addition

1. Connect to PostgreSQL:
```bash
docker-compose exec postgres psql -U postgres -d lang_assessment
```

2. Insert a question:
```sql
INSERT INTO question (language_id, module_type, cefr_level, question_text, question_number, status, created_at, updated_at)
VALUES (1, 'LISTENING', 'INTERMEDIATE', 'Your question here', 1, 'ACTIVE', NOW(), NOW());
```

## Creating Assessments

1. Create an assessment via Admin Dashboard
2. Add modules (Listening, Reading, etc.) with desired number of questions
3. The system will automatically select matching questions from the `question` table

**Important:** If there aren't enough questions in the database for the configuration, candidates will see "No Questions Available".

## Example Data Structure

### Assessment Configuration
- Assessment: "English Test 1"
- Language: English
- Modules:
  - LISTENING: 10 questions
  - READING: 10 questions
  - WRITING: 5 questions
  - SPOKEN_INTERACTION: 5 questions
  - SPOKEN_PRODUCTION: 5 questions

### Required Questions in Database
For the above configuration, you need:
- ≥ 10 LISTENING questions in English
- ≥ 10 READING questions in English
- ≥ 5 WRITING questions in English
- ≥ 5 SPOKEN_INTERACTION questions in English
- ≥ 5 SPOKEN_PRODUCTION questions in English

## Future Enhancement: AI Question Generation

The system has an `evaluation-service` (Python/Gemini) that can be extended to auto-generate questions based on:
- Assessment topic
- CEFR level
- Module type
- Language

This would eliminate the need for pre-seeded questions.
