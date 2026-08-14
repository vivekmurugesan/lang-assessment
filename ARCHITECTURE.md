# Language Assessment System - Architecture

## System Overview

The Language Assessment System is a comprehensive platform for evaluating candidates on language proficiency based on CEFR (Common European Framework of Reference) levels. The system uses a modern microservices architecture with separate frontend, backend API, and evaluation services.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Client Layer (Browser)                       │
│                                                                       │
│  ┌──────────────────────────────────┐   ┌──────────────────────┐   │
│  │   Admin Interface (React)         │   │ Candidate Portal     │   │
│  │  - Assessment Setup               │   │ - Assessment Taker   │   │
│  │  - Candidate Management           │   │ - Results Viewer     │   │
│  │  - Monitoring & Reports           │   │                      │   │
│  └──────────────────────────────────┘   └──────────────────────┘   │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                    HTTP/WebSocket (Port 3000)
                                 │
┌────────────────────────────────▼────────────────────────────────────┐
│                    Frontend Layer (React 18)                         │
│                                                                       │
│  - Component-based architecture                                      │
│  - Zustand state management                                          │
│  - React Query for data fetching                                     │
│  - Responsive design with Tailwind CSS                               │
│  - JWT-based authentication                                          │
│  - WebSocket for real-time updates                                   │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                    REST API (Port 8080)
                                 │
┌────────────────────────────────▼────────────────────────────────────┐
│              Backend API Layer (Spring Boot 3)                       │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  REST Controllers                                            │   │
│  │  - AuthController (/api/auth)                               │   │
│  │  - AssessmentController (/api/assessments)                  │   │
│  │  - QuestionController (/api/questions)                      │   │
│  │  - SubmissionController (/api/submissions)                  │   │
│  │  - ReportController (/api/reports)                          │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  Services & Business Logic                                   │   │
│  │  - UserService                                               │   │
│  │  - AssessmentService                                         │   │
│  │  - QuestionService                                           │   │
│  │  - SubmissionService                                         │   │
│  │  - StorageService (MinIO integration)                        │   │
│  │  - EvaluationService                                         │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  Security & Authentication                                   │   │
│  │  - JWT Token Provider                                        │   │
│  │  - Password Encoder (BCrypt)                                 │   │
│  │  - Role-Based Access Control (RBAC)                          │   │
│  │  - JWT Authentication Filter                                 │   │
│  └─────────────────────────────────────────────────────────────┘   │
└────────────────────────────────┬────────────────────────────────────┘
         │                        │                        │
         │                        │                        │
      REST API              Async HTTP           gRPC/HTTP
       (Port 8080)          (Port 5000)          
         │                        │                        │
┌────────▼──────┐    ┌────────────▼─────────────┐    ┌────▼─────────┐
│  PostgreSQL   │    │ Evaluation Service       │    │  MinIO (S3)  │
│  (Port 5432)  │    │ (Python Flask)           │    │ (Port 9000)  │
│               │    │                          │    │              │
│  - Users      │    │ ┌────────────────────┐   │    │ - Audio      │
│  - Assessments│    │ │Evaluation Engine   │   │    │ - Images     │
│  - Questions  │    │ └────────────────────┘   │    │ - Submissions│
│  - Submissions│    │                          │    │ - Content    │
│  - Results    │    │ ┌────────────────────┐   │    │              │
│  - Scorecard  │    │ │Content Curator     │   │    │ Buckets:     │
│  - Audit Log  │    │ └────────────────────┘   │    │ - assessments│
│               │    │                          │    │ - submissions│
└───────────────┘    │ ┌────────────────────┐   │    └──────────────┘
                     │ │AI Model Adapter    │   │
                     │ ├────────────────────┤   │
                     │ │ - Gemini API       │   │
                     │ │ - Translation      │   │
                     │ │ - Content Generat. │   │
                     │ └────────────────────┘   │
                     │                          │
                     │ Logging & Monitoring    │
                     │ - Request Logging       │
                     │ - Error Tracking        │
                     │ - Performance Metrics   │
                     └──────────────────────────┘
                              │
                         Google Cloud
                              │
                     ┌────────▼────────┐
                     │ Gemini API      │
                     │ - Evaluation    │
                     │ - Translation   │
                     │ - Content Gen.  │
                     └─────────────────┘
```

## Component Details

### Frontend Layer (React 18)

**Technologies**: React, Zustand, React Query, Tailwind CSS

**Key Features**:
- Single Page Application (SPA)
- Component-based architecture
- State management with Zustand
- Server state management with React Query
- Responsive design for desktop and tablet
- JWT-based authentication
- Real-time updates via WebSocket

**Directory Structure**:
```
frontend/
├── src/
│   ├── components/        # Reusable UI components
│   ├── pages/             # Page components
│   │   ├── admin/         # Admin pages
│   │   ├── candidate/     # Candidate pages
│   │   └── auth/          # Auth pages
│   ├── store/             # Zustand stores
│   ├── api/               # Axios configuration
│   ├── hooks/             # Custom React hooks
│   ├── utils/             # Utility functions
│   └── styles/            # Global styles
├── public/                # Static assets
└── package.json
```

### Backend API Layer (Spring Boot 3)

**Technologies**: Spring Boot, Spring Data JPA, Spring Security, PostgreSQL

**Key Features**:
- RESTful API design
- Entity-Relationship Model (ORM)
- JWT-based authentication
- Role-Based Access Control (RBAC)
- Database connection pooling (HikariCP)
- OpenAPI/Swagger documentation
- Health checks and metrics

**Module Structure**:
```
backend/
├── src/main/java/com/langassessment/
│   ├── entity/            # JPA entities
│   ├── dto/               # Data Transfer Objects
│   ├── service/           # Business logic
│   ├── repository/        # Data access layer
│   ├── controller/        # REST controllers
│   ├── security/          # Security components
│   ├── config/            # Configuration classes
│   └── exception/         # Custom exceptions
├── src/main/resources/
│   ├── application.yml    # Configuration
│   └── db/migration/      # Flyway migrations
└── pom.xml
```

### Evaluation Service (Python Flask)

**Technologies**: Flask, Google Gemini API, scikit-learn

**Key Features**:
- Modular evaluation engine
- AI-powered assessment using Gemini API
- Rule-based fallback evaluation
- Content generation and translation
- Audio processing for speaking sections
- Token usage tracking and billing

**Service Components**:
```
evaluation-service/
├── services/
│   ├── evaluation_engine.py      # Core evaluation logic
│   ├── ai_adapter.py             # AI model integration
│   ├── content_curator.py        # Content management
│   └── audio_processor.py        # Audio analysis
├── app.py                        # Flask application
├── requirements.txt
└── Dockerfile
```

### Data Storage Layer

**PostgreSQL Database**:
- Relational database for structured data
- ACID compliance
- JSON support for flexible schemas
- Full-text search capabilities
- Partitioning for large tables (audit logs, submissions)

**MinIO Object Storage**:
- S3-compatible object storage
- Immutable audit trail
- Lifecycle policies for old submissions
- Access controls and versioning
- Suitable for audio, images, and submission artifacts

## Data Models

### Core Entities

```
User
├── id (PK)
├── email (Unique)
├── name
├── password_hash
├── role (ADMIN/CANDIDATE)
└── created_at, updated_at

Assessment
├── id (PK)
├── admin_id (FK → User)
├── language_id (FK → Language)
├── title
├── description
├── status (DRAFT/ACTIVE/CLOSED/ARCHIVED)
└── timestamps

Assessment_Modules
├── id (PK)
├── assessment_id (FK)
├── module_type (LISTENING/READING/WRITING/SPEAKING)
├── num_questions
├── difficulty_level
└── is_enabled

Question
├── id (PK)
├── language_id (FK)
├── module_type
├── cefr_level
├── question_text
├── status (ACTIVE/INACTIVE/UNDER_REVIEW)
└── timestamps

Submission
├── id (PK)
├── assessment_candidate_id (FK)
├── question_id (FK)
├── answer_text / answer_audio_path
└── submission_time

Evaluation_Results
├── id (PK)
├── submission_id (FK)
├── assessment_candidate_id (FK)
├── module_type
├── score, cefr_level
├── metrics (JSON)
├── reasoning
└── evaluation_timestamp

Scorecard
├── id (PK)
├── assessment_candidate_id (FK, Unique)
├── {listening,reading,writing,speaking}_{score,level}
├── overall_score, overall_level
└── evaluation_completed_at
```

## Authentication & Authorization

### Authentication Flow

1. **User Login**
   ```
   User Email/Password
        ↓
   Spring Security Validation
        ↓
   JWT Token Generation
        ↓
   Token stored in localStorage
   ```

2. **Request Authorization**
   ```
   HTTP Request + Bearer Token
        ↓
   JwtAuthenticationFilter
        ↓
   Token Validation
        ↓
   User Context in SecurityContext
   ```

### Role-Based Access Control

```
ADMIN Role:
├── /api/admin/assessments/* (CRUD)
├── /api/admin/candidates/* (Manage)
├── /api/admin/evaluation/* (Trigger/Review)
├── /api/admin/reports/* (View)
└── /api/admin/content/* (Manage)

CANDIDATE Role:
├── /api/candidate/assessment/{id} (Take)
├── /api/candidate/submissions/* (View own)
└── /api/candidate/results/* (View own)
```

## Assessment Workflow

```
1. Admin Setup
   ├── Create Assessment
   ├── Select Language & CEFR Level
   ├── Configure Modules (Listening/Reading/Writing/Speaking)
   └── Add Questions & Content

2. Candidate Onboarding
   ├── Generate Secure Link
   ├── Generate Temporary Password
   ├── Send to Candidates via Email
   └── Track Invitation Status

3. Assessment Taking
   ├── Login with Secure Link
   ├── Start Assessment
   ├── Complete Each Module
   │   ├── Listen to Audio (Listening)
   │   ├── Read Text (Reading)
   │   ├── Record Audio (Speaking)
   │   └── Type Response (Writing)
   ├── Submit Answers
   └── Receive Confirmation

4. Evaluation
   ├── Backend triggers Evaluation Service
   ├── AI-based evaluation for each section
   ├── Generate scores and CEFR level
   ├── Create scorecard
   └── Store results & audit trail

5. Results & Reporting
   ├── Admin reviews results
   ├── Generate performance reports
   ├── Set qualification thresholds
   └── Export results for further processing
```

## Security Architecture

### Layers of Security

```
Application Layer
├── HTTPS/TLS encryption in transit
├── CORS policy enforcement
├── CSRF token validation
└── Rate limiting per endpoint

Authentication Layer
├── JWT token-based auth
├── Secure password hashing (BCrypt)
├── Token expiration (24 hours)
└── Refresh token mechanism

Authorization Layer
├── Role-Based Access Control (RBAC)
├── Method-level security annotations
├── Resource ownership validation
└── Data isolation by candidate

Data Layer
├── Encrypted credentials storage
├── Database user with minimal privileges
├── SQL injection prevention (Prepared statements)
├── PII data masking in logs
└── Audit trail for all changes
```

## Performance Considerations

### Caching Strategy

```
Frontend Caching:
├── HTTP cache headers (ETags)
├── localStorage for user data
├── sessionStorage for form state
└── React Query for server state

Backend Caching:
├── Redis for session data
├── Query result caching
├── Content caching for questions
└── Evaluation result caching
```

### Database Optimization

```
Indexing:
├── Users: email, role
├── Assessments: admin_id, language_id, status
├── Submissions: assessment_candidate_id, created_at
├── Evaluation_Results: assessment_candidate_id, cefr_level
└── Full-text indexes for search

Query Optimization:
├── Lazy loading for relationships
├── Pagination for large result sets
├── Connection pooling (HikariCP)
└── Prepared statements

Partitioning:
├── Audit logs by date
├── Submissions by assessment_id
└── Evaluation results by month
```

### Load Balancing

```
Multiple Backend Instances
        ↓
Load Balancer (Nginx)
    ├── Round-robin routing
    ├── Health checks
    ├── Sticky sessions for auth
    └── SSL termination

Multiple Evaluation Service Instances
        ↓
Queue-based Distribution
    ├── Task queue (Celery/RabbitMQ)
    ├── Priority-based processing
    └── Scaling based on demand
```

## Deployment Architecture

### Development Environment

```
Docker Compose (Single Docker Engine)
├── Frontend Container (Node)
├── Backend Container (Java)
├── PostgreSQL Container
├── MinIO Container
├── Evaluation Service Container (Python)
└── All containers on shared Docker network
```

### Production Environment

```
Kubernetes Cluster
├── Frontend Pod (Replicated)
├── Backend Pod (Replicated)
├── Evaluation Service Pod (Replicated)
├── PostgreSQL StatefulSet
├── MinIO StatefulSet
├── Redis Cache
├── Prometheus Monitoring
├── ELK Stack Logging
└── Ingress Controller (HTTPS)
```

## Technology Decisions & Rationale

| Decision | Alternative | Rationale |
|----------|-------------|-----------|
| React | Vue/Angular | Large ecosystem, component reuse, strong typing with TypeScript |
| Spring Boot | Django/FastAPI | Enterprise-grade, mature ecosystem, excellent security features |
| PostgreSQL | MongoDB | ACID compliance, relational data model, excellent indexing |
| MinIO | AWS S3 | Self-hosted option, S3-compatible, suitable for cost control |
| JWT | Session cookies | Stateless auth, suitable for microservices, mobile-friendly |
| Gemini API | OpenAI/Claude | Cost-effective, multilingual, good for content generation |

## Future Enhancements

1. **Machine Learning**
   - Custom ML models for evaluation
   - Personalized learning recommendations
   - Anomaly detection for cheating

2. **Real-time Collaboration**
   - Speaking assessment with live grading
   - Video interview support
   - Interactive listening sessions

3. **Mobile Application**
   - Native iOS/Android apps
   - Offline assessment taking
   - Audio quality optimization

4. **Advanced Reporting**
   - Analytics dashboard
   - Performance trends
   - Comparative analysis

5. **Multi-tenancy**
   - Organization-based assessment management
   - Customizable branding
   - Advanced reporting per organization
