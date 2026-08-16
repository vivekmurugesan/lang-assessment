# AI-Powered Question Generation with Gemini API

Complete guide for using Google Gemini API to auto-generate assessment questions with admin review workflow.

## System Architecture

```
Admin Dashboard
    ↓
[1] Click "Generate Questions"
    ↓
Question Generation Service (Gemini API)
    ├─ Calls Google Gemini API
    ├─ Generates questions for each module
    └─ Stores with PENDING_REVIEW status
    ↓
MinIO Storage
    ├─ Audio files (listening questions)
    └─ Images (reading questions)
    ↓
[2] Admin Reviews Questions
    ├─ Question Review Page
    ├─ Approve or Reject each question
    └─ Questions marked as ACTIVE/REJECTED
    ↓
Candidate Takes Assessment
    └─ Only ACTIVE questions are shown
    ↓
Evaluation Service (Gemini API)
    ├─ Scores multiple-choice answers
    ├─ Evaluates open-ended responses
    ├─ Determines CEFR level
    └─ Provides feedback
    ↓
Results Dashboard
    └─ Score, CEFR level, feedback displayed
```

## Prerequisites

1. **Google Gemini API Key**
   - Get from: https://makersuite.google.com/app/apikey
   - Free tier available (60 requests/min)

2. **MinIO Setup** (for media storage)
   - Already configured in docker-compose.yml
   - Default: `http://minio:9000`

3. **Environment Variables**
   ```bash
   GEMINI_API_KEY=your-gemini-api-key-here
   MINIO_ENDPOINT=http://minio:9000
   MINIO_ACCESS_KEY=minioadmin
   MINIO_SECRET_KEY=minioadmin123
   ```

## Configuration

### 1. Add Environment Variables

Update your `.env` file:

```bash
# Google Gemini API
GEMINI_API_KEY=your-actual-api-key

# MinIO (default values work for Docker)
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin123
MINIO_BUCKET_QUESTIONS=questions
```

### 2. Update .env in Docker

Add to docker-compose.yml backend service:

```yaml
environment:
  GEMINI_API_KEY: ${GEMINI_API_KEY}
  MINIO_ENDPOINT: ${MINIO_ENDPOINT}
```

### 3. Restart Services

```bash
docker-compose down
docker-compose up -d
```

## Workflow: Complete Example

### Step 1: Create Assessment

1. Admin Dashboard → Assessments → Create New Assessment
2. Configure assessment:
   - **Title**: "English Proficiency Test"
   - **Language**: English
   - **CEFR Level**: INTERMEDIATE
   - **Modules**:
     - LISTENING: 10 questions
     - READING: 10 questions
     - WRITING: 5 questions
     - SPOKEN_INTERACTION: 5 questions
     - SPOKEN_PRODUCTION: 5 questions

### Step 2: Generate Questions

API Endpoint:
```http
POST /api/admin/questions/generate/{assessmentId}
Authorization: Bearer {admin-token}
```

Example using curl:
```bash
curl -X POST http://localhost:8080/api/admin/questions/generate/1 \
  -H "Authorization: Bearer your-token"
```

Response:
```json
{
  "success": true,
  "message": "Generated 35 questions successfully",
  "data": [
    {
      "id": 1001,
      "questionText": "What is the main topic of the audio?",
      "moduleType": "LISTENING",
      "cefrLevel": "INTERMEDIATE",
      "options": ["A", "B", "C", "D"],
      "correctAnswer": "B",
      "explanation": "The speaker clearly states...",
      "status": "PENDING_REVIEW",
      "generatedBy": "GEMINI_API"
    },
    ...
  ]
}
```

### Step 3: Review Questions

1. **Admin Dashboard** → Assessments → [Select Assessment] → **"Review Questions"**
2. For each question:
   - **Review** the question text, options, and explanation
   - **Listen** to audio (if LISTENING module)
   - **View** images (if READING module)
   - **Approve** (mark ACTIVE) or **Reject** (mark REJECTED)
   - **Add notes** (optional) for your records

API Endpoints:

**Get Pending Questions:**
```http
GET /api/admin/questions/review/{assessmentId}
```

**Approve Question:**
```http
POST /api/admin/questions/{questionId}/approve
Body: {
  "notes": "Looks good, grammar is correct"
}
```

**Reject Question:**
```http
POST /api/admin/questions/{questionId}/reject
Body: {
  "reason": "Question is ambiguous, has multiple valid answers"
}
```

### Step 4: Onboard Candidates

After approving all questions:
1. Admin Dashboard → Candidate Onboarding
2. Add candidates with email addresses
3. Email sent with secure link
4. Candidates click link and login

### Step 5: Candidate Takes Assessment

1. Candidate receives email with secure link and password
2. Clicks link → enters password → assessment starts
3. Views **only ACTIVE questions** (pre-approved by admin)
4. Completes all sections:
   - **LISTENING**: Hear audio, select answer
   - **READING**: Read text, select answer
   - **WRITING**: Type essay response
   - **SPOKEN_INTERACTION**: Record audio response
   - **SPOKEN_PRODUCTION**: Record speaking

### Step 6: Automatic Evaluation

When candidate submits:

```
Submission Received
    ↓
EvaluationService.evaluateSubmission() called
    ├─ Score multiple-choice questions
    │  ├─ LISTENING answers
    │  └─ READING answers
    ├─ Evaluate open-ended responses
    │  ├─ Call Gemini API for WRITING feedback
    │  ├─ Call Gemini API for SPEAKING feedback
    │  └─ Gemini evaluates fluency, grammar, vocabulary
    ├─ Calculate total score (0-100)
    ├─ Determine CEFR level:
    │  - 90-100: C2 (Mastery)
    │  - 80-89: C1 (Proficiency)
    │  - 70-79: B2 (Upper Intermediate)
    │  - 60-69: B1 (Intermediate)
    │  - 50-59: A2 (Elementary)
    │  - <50: A1 (Beginner)
    └─ Mark as EVALUATED
    ↓
Results Available
```

**Evaluation Prompt Example (for WRITING):**
```
Evaluate this candidate's response to a language assessment question.

Question: Write an email to a friend about your recent vacation.
Question Type: WRITING
CEFR Level: INTERMEDIATE

Candidate's Response: "I just came back from Paris. It was amazing! 
The weather was perfect, and I visited the Eiffel Tower. The food 
was delicious. I recommend everyone to visit Paris. It's the best 
city in the world. Please come with me next time."

Provide a brief evaluation covering:
1. Correctness and completeness
2. Grammar and language use
3. Strengths and areas for improvement
```

## Question Storage in MinIO

Questions with audio/image are stored in MinIO:

```
questions/
├── audio/
│   ├── uuid_listening_q1.wav
│   ├── uuid_listening_q2.wav
│   └── ...
├── images/
│   ├── uuid_reading_q1.png
│   ├── uuid_reading_q2.jpg
│   └── ...
└── ...
```

URLs are stored in Question entity:
- `audioUrl`: `http://minio:9000/questions/audio/uuid_...wav`
- `imageUrl`: `http://minio:9000/questions/images/uuid_...png`

## API Reference

### Question Generation

**Endpoint:**
```http
POST /api/admin/questions/generate/{assessmentId}
Authorization: Bearer {admin-token}
Content-Type: application/json
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Generated 35 questions successfully",
  "data": [
    {
      "id": 1001,
      "questionText": "...",
      "moduleType": "LISTENING",
      "cefrLevel": "INTERMEDIATE",
      "options": ["A", "B", "C", "D"],
      "correctAnswer": "B",
      "explanation": "...",
      "audioUrl": "http://minio:9000/questions/audio/...",
      "status": "PENDING_REVIEW",
      "generatedAt": "2026-08-16T10:30:00",
      "generatedBy": "GEMINI_API"
    }
  ]
}
```

### Review Pending Questions

**Endpoint:**
```http
GET /api/admin/questions/review/{assessmentId}
Authorization: Bearer {admin-token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Questions retrieved",
  "data": [...]
}
```

### Approve Question

**Endpoint:**
```http
POST /api/admin/questions/{questionId}/approve
Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "notes": "Approved - good quality question"
}
```

### Reject Question

**Endpoint:**
```http
POST /api/admin/questions/{questionId}/reject
Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "reason": "Question text is unclear, ambiguous options"
}
```

## Gemini API Prompts

### Question Generation Prompt

```
Generate 10 high-quality language assessment questions for the LISTENING module in English.
Assessment: English Proficiency Test
Description: Comprehensive language proficiency evaluation
Language Level: INTERMEDIATE

For LISTENING module, create questions that:
- Are appropriate for INTERMEDIATE learners
- Test comprehension and language skills
- Have clear answers
- Each question should have corresponding audio content

Return ONLY a JSON array with format:
[
  {
    "questionText": "What is the main topic of the audio?",
    "type": "multiple-choice",
    "options": ["Option A", "Option B", "Option C", "Option D"],
    "correctAnswer": "A",
    "explanation": "The speaker clearly mentions..."
  }
]
```

### Evaluation Prompt

```
Evaluate this candidate's response to a language assessment question.

Question: Write an email to a friend describing your favorite place.
Question Type: WRITING
CEFR Level: INTERMEDIATE

Candidate's Response: "[candidate's actual response]"

Provide a brief evaluation (2-3 sentences) covering:
1. Correctness and completeness
2. Grammar and language use
3. Strengths and areas for improvement

Format as: "Assessment: [your evaluation]"
```

## Question Status Flow

```
[PENDING_REVIEW] ← Generated by Gemini
       ↓
   Admin reviews
       ├─ Approves → [ACTIVE] → Candidates see this
       └─ Rejects → [REJECTED] → Candidates don't see this
```

## CEFR Level Determination Algorithm

```python
def determine_cefr_level(score):
    if score >= 90:
        return "C2"  # Mastery
    elif score >= 80:
        return "C1"  # Proficiency
    elif score >= 70:
        return "B2"  # Upper Intermediate
    elif score >= 60:
        return "B1"  # Intermediate
    elif score >= 50:
        return "A2"  # Elementary
    else:
        return "A1"  # Beginner
```

## Troubleshooting

### Issue: Gemini API returns error

**Error:** `Failed to generate questions: API call failed`

**Solution:**
1. Verify `GEMINI_API_KEY` is set correctly
2. Check API key is active: https://makersuite.google.com/app/apikey
3. Check rate limits (60 requests/min for free tier)
4. Verify internet connection

### Issue: MinIO media upload fails

**Error:** `Failed to upload audio to MinIO`

**Solution:**
1. Verify MinIO is running: `docker-compose ps | grep minio`
2. Check MinIO endpoint: `MINIO_ENDPOINT=http://minio:9000`
3. Verify credentials match docker-compose.yml
4. Check MinIO console: `http://localhost:9001` (default: minioadmin/minioadmin123)

### Issue: Questions not showing in review

**Cause:** Questions have wrong status or language

**Solution:**
1. Check question status: `SELECT status FROM question WHERE id = ?;`
2. Verify assessment language matches question language
3. Check question module types match assessment modules

### Issue: Evaluation not triggering

**Cause:** EvaluationService error during submission

**Solution:**
1. Check backend logs: `docker-compose logs backend | grep -i evaluation`
2. Verify Gemini API key for evaluation
3. Check response format from candidate
4. Manually trigger: `POST /api/admin/submissions/{submissionId}/evaluate`

## Cost Estimation

**Google Gemini API Pricing:**
- Free tier: 60 requests/minute
- 1 million tokens = ~$0.075 (input), $0.30 (output)

**Example costs:**
- 1 Assessment (35 questions): ~$0.10-0.15
- 100 Assessments: ~$10-15
- Student evaluation (1 submission): ~$0.05-0.10

## Next Steps

1. [x] Deploy system with Gemini integration
2. [x] Create assessment with AI-generated questions
3. [x] Admin reviews and approves questions
4. [x] Onboard candidates
5. [x] Candidates take assessment
6. [x] Automatic evaluation with CEFR level
7. [ ] (Optional) Implement question regeneration if rejected
8. [ ] (Optional) Add question quality metrics
9. [ ] (Optional) Track Gemini API usage and costs

## References

- [Google Gemini API Docs](https://ai.google.dev/docs)
- [MinIO Documentation](https://min.io/docs/minio/kubernetes/upstream/)
- [CEFR Framework](https://www.coe.int/en/web/common-european-framework-reference-languages)
