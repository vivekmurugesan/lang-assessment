# Getting Started with Language Assessment System

## Quick Start (5 minutes)

### Prerequisites
- Docker and Docker Compose installed
- Port 3000, 5000, 8080, 5432, 9000, and 9001 available

### Start the Application

```bash
# 1. Navigate to project directory
cd /home/user/lang-assessment

# 2. Configure environment (update .env if needed)
# Default credentials are already set up

# 3. Start all services
docker-compose up -d

# 4. Wait for services to be healthy (30-40 seconds)
docker-compose ps

# 5. Access the application
# - Frontend: http://localhost:3000
# - Admin API: http://localhost:8080/swagger-ui.html
# - MinIO Console: http://localhost:9001
```

### First Login

**Admin User:**
- Email: `admin@langassessment.com`
- Password: `password` (Change in production!)

**Test the Application:**
1. Login as admin
2. Navigate to "Assessments" section
3. Create a new assessment
4. Configure modules (Listening, Reading, Writing, Speaking)
5. Onboard candidates with test email addresses
6. Review secure links generated for candidates

## Project Structure

### Frontend (`/frontend`)
React 18 application with:
- Authentication pages (login, candidate login)
- Admin dashboard and management interfaces
- Candidate assessment portal
- Responsive design with Tailwind CSS
- Zustand state management

**Key Files:**
- `src/App.jsx` - Main application router
- `src/pages/auth/LoginPage.jsx` - Admin login
- `src/pages/admin/AdminDashboard.jsx` - Admin home
- `src/store/authStore.js` - Authentication state

### Backend (`/backend`)
Spring Boot 3 RESTful API with:
- User authentication (JWT)
- Assessment management
- Candidate onboarding
- Question and content management
- Submission handling
- Integration with evaluation service

**Key Files:**
- `src/main/java/com/langassessment/controller/` - REST endpoints
- `src/main/java/com/langassessment/entity/` - Data models
- `src/main/java/com/langassessment/service/` - Business logic
- `src/main/resources/application.yml` - Configuration

### Database (`/database`)
PostgreSQL database with:
- 16+ tables for complete data model
- CEFR level definitions
- User and role management
- Assessment configuration
- Submission tracking
- Evaluation results and scorecards
- Audit logging

**Files:**
- `schema.sql` - Database schema definition
- `seed.sql` - Initial data (languages, CEFR levels, users)
- `init.sql` - Database initialization

### Evaluation Service (`/evaluation-service`)
Python Flask service for AI-powered evaluation:
- CEFR-based scoring algorithms
- Google Gemini API integration
- Content generation and translation
- Audio processing for speaking sections
- Rule-based evaluation engine

**Key Files:**
- `app.py` - Flask application
- `services/evaluation_engine.py` - Core evaluation logic
- `services/ai_adapter.py` - AI model integration
- `services/content_curator.py` - Content management

### Infrastructure
- `docker-compose.yml` - Complete service orchestration
- `.env` - Environment configuration
- `Dockerfile` files for each service

## Architecture Overview

```
┌─────────────────────────────────────────────────┐
│  Frontend (React 18) - Port 3000                │
│  ├─ Admin Dashboard                             │
│  └─ Candidate Portal                            │
└──────────────────┬──────────────────────────────┘
                   │ REST API
┌──────────────────▼──────────────────────────────┐
│  Backend (Spring Boot) - Port 8080              │
│  ├─ Authentication & Authorization              │
│  ├─ Assessment Management                       │
│  ├─ Candidate Onboarding                        │
│  ├─ Question Management                         │
│  └─ Submission Handling                         │
└──────────────┬──────────────────┬───────────────┘
               │                  │
          ┌────▼───────┐   ┌────▼────────────┐
          │ PostgreSQL  │   │ Evaluation      │
          │ Port 5432   │   │ Service         │
          │             │   │ Port 5000       │
          │ - Data      │   │                 │
          │ - Users     │   │ - Evaluation    │
          │ - Results   │   │ - Translation   │
          └─────────────┘   │ - Content Gen.  │
                            └────┬────────────┘
                                 │
                            ┌────▼──────────┐
                            │ Gemini API    │
                            │ - Evaluation  │
                            │ - Translation │
                            └───────────────┘

┌──────────────────────────────────────────────────┐
│  MinIO (S3-compatible) - Port 9000               │
│  └─ Media Storage (Audio, Images, Submissions)   │
└──────────────────────────────────────────────────┘
```

## Key Features

### Admin Features
✅ Create and manage assessments
✅ Configure assessment modules (5 types)
✅ Bulk onboard candidates
✅ Generate secure test links
✅ Monitor candidate progress
✅ Trigger evaluations
✅ View detailed reports
✅ Manage assessment content
✅ Generate questions in different languages
✅ Translate content to multiple languages

### Candidate Features
✅ Secure login with unique link
✅ Complete language assessments
✅ Listen to audio in listening section
✅ Read text in reading section
✅ Record audio for speaking section
✅ Type responses in writing section
✅ Get immediate feedback
✅ View final scores and CEFR level
✅ Review detailed results

## Development Workflow

### Making Changes to Frontend

```bash
cd frontend
npm install              # Install dependencies
npm start               # Start dev server (auto-reload)
npm run build           # Build for production
```

### Making Changes to Backend

```bash
cd backend
./mvnw clean install    # Build and test
./mvnw spring-boot:run  # Run in development
# IDE: Open in IntelliJ or VS Code with Spring extension
```

### Making Changes to Evaluation Service

```bash
cd evaluation-service
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python app.py           # Run development server
```

## Deployment

See `DEPLOYMENT.md` for comprehensive production deployment instructions.

## API Documentation

The backend provides OpenAPI/Swagger documentation at:
```
http://localhost:8080/swagger-ui.html
```

## Database Management

### Access PostgreSQL

```bash
docker exec -it lang-assessment-postgres psql -U postgres -d lang_assessment
```

### Common Queries

```sql
-- View all users
SELECT * FROM users;

-- View assessments
SELECT * FROM assessments;

-- View evaluation results
SELECT * FROM evaluation_results;

-- Check submission status
SELECT * FROM submissions WHERE assessment_candidate_id = 1;
```

## Monitoring

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f evaluation-service
```

### Health Checks

```bash
# Backend
curl http://localhost:8080/actuator/health

# Evaluation Service
curl http://localhost:5000/health

# MinIO
curl http://localhost:9000/minio/health/live
```

## Troubleshooting

### Port Already in Use

```bash
# Find and kill process on port 8080
lsof -i :8080
kill -9 <PID>
```

### Database Connection Failed

```bash
# Check PostgreSQL status
docker exec lang-assessment-postgres pg_isready

# View logs
docker logs lang-assessment-postgres
```

### Frontend Not Loading

```bash
# Clear node modules and reinstall
cd frontend
rm -rf node_modules
npm install
npm start
```

### Evaluation Service Issues

```bash
# Check Python environment
docker exec lang-assessment-evaluation python -V

# View service logs
docker logs lang-assessment-evaluation

# Restart service
docker restart lang-assessment-evaluation
```

## Security Considerations

⚠️ **Important for Production:**

1. Change all default passwords in `.env`
2. Generate a strong JWT secret (32+ characters)
3. Enable HTTPS with valid SSL certificates
4. Configure CORS with specific allowed origins
5. Set up API rate limiting
6. Enable database encryption at rest
7. Use secrets management service (AWS Secrets Manager, HashiCorp Vault)
8. Configure firewall rules
9. Regular security updates for base images
10. Enable audit logging

## Next Steps

1. **Customize the Application**
   - Update branding and styling
   - Add organization-specific features
   - Integrate with existing systems

2. **Populate Content**
   - Add assessment questions
   - Upload audio files
   - Configure content in different languages

3. **User Management**
   - Set up multiple admin users
   - Configure admin roles and permissions
   - Implement organization hierarchies

4. **Integration**
   - Connect to email service for notifications
   - Integrate with authentication provider (OAuth2)
   - Setup webhook endpoints for external systems

5. **Analytics & Reporting**
   - Configure dashboard metrics
   - Set up performance monitoring
   - Generate periodic reports

## Support & Documentation

- **Architecture Guide:** See `ARCHITECTURE.md`
- **Deployment Guide:** See `DEPLOYMENT.md`
- **API Documentation:** http://localhost:8080/swagger-ui.html
- **Database Schema:** See `database/schema.sql`

## Environment Variables

Key environment variables in `.env`:

```env
# Database
DB_HOST=postgres
DB_PORT=5432
DB_NAME=lang_assessment
DB_USER=postgres
DB_PASSWORD=password123

# MinIO Storage
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin123

# JWT Security
JWT_SECRET=your-secret-key-min-32-chars
JWT_EXPIRATION=86400000

# AI Integration
GEMINI_API_KEY=your-gemini-key

# Services
EVALUATION_SERVICE_URL=http://evaluation-service:5000
REACT_APP_API_URL=http://localhost:8080/api
```

## License

This project is licensed under the MIT License.

---

**Ready to start?** Run `docker-compose up -d` and visit http://localhost:3000!
