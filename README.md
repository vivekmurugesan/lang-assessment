# Language Assessment System

A comprehensive system for evaluating candidates on language proficiency based on CEFR (Common European Framework of Reference) levels.

## System Architecture

The system consists of the following components:

- **Frontend**: React-based UI for both admin and candidate personas
- **Backend API**: Spring Boot REST API service
- **Database**: PostgreSQL for structured data, MinIO for unstructured media
- **Evaluation Engine**: Python-based evaluation system with AI integration
- **Content Curation**: Automated content generation and management

## Features

### Admin Users
- Setup and configure language assessments
- Bulk onboard candidates with unique secure links
- Monitor candidate progress and completion
- View detailed reports and analytics
- Trigger evaluation of submissions
- Manage assessment content and translations

### Candidates
- Authenticate with secure credentials
- Complete assessments across 5 sections:
  - Listening (with audio)
  - Reading (with text)
  - Spoken Interaction (recording)
  - Spoken Production (recording)
  - Writing (text input)
- Submit answers and receive evaluations

## Technical Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Frontend | React 18+ | Responsive UI for admin and candidates |
| Backend | Spring Boot 3+ | RESTful API service |
| Database | PostgreSQL 14+ | Structured data storage |
| Media Storage | MinIO/S3 | Unstructured data (audio, submissions) |
| Evaluation | Python 3.9+ | AI-powered assessment evaluation |
| Security | JWT + OAuth2 | Authentication and authorization |
| Containerization | Docker | Easy deployment |

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17+
- Node.js 18+
- Python 3.9+
- PostgreSQL 14+ (if running locally)

### Running with Docker Compose

```bash
docker-compose up -d
```

This will start:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- PostgreSQL: localhost:5432
- MinIO: http://localhost:9000

### Development Setup

```bash
# Backend
cd backend
./mvnw clean install
./mvnw spring-boot:run

# Frontend
cd frontend
npm install
npm start

# Evaluation Service
cd evaluation-service
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python app.py
```

## Project Structure

```
lang-assessment/
├── frontend/                 # React frontend
├── backend/                  # Spring Boot backend
├── evaluation-service/       # Python evaluation engine
├── database/                 # Database scripts
├── docker-compose.yml        # Docker compose configuration
└── docs/                     # Documentation
```

## API Documentation

Available at: `http://localhost:8080/swagger-ui.html`

## Configuration

### Environment Variables

Environment variables can be configured via:
- `.env` file (default: development settings)
- `docker-compose.yml` (production with Docker)
- Backend: `application.yml`
- Frontend: `config.js`

### Email Configuration (Optional)

To enable candidate invitation emails, configure SMTP settings in `.env`:

```bash
# Gmail Example (with App Password)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@langassessment.com
```

**For Gmail:**
1. Enable 2-Factor Authentication on your Google Account
2. Generate an [App Password](https://myaccount.google.com/apppasswords)
3. Use the 16-character app password in `MAIL_PASSWORD`

**For Other Email Providers:**
- **Outlook**: `smtp.office365.com:587`
- **SendGrid**: `smtp.sendgrid.net:587`
- **AWS SES**: `email-smtp.{region}.amazonaws.com:587`

**Note:** Email sending is optional. If not configured, the system will start normally and skip sending invitations. Candidates can still access assessments via direct secure links.

## Security

- All endpoints protected with JWT tokens
- Role-based access control (RBAC)
- Candidates isolated from admin functions
- Secure password hashing (BCrypt)
- CORS configured for cross-origin requests

## License

Licensed under MIT License.
