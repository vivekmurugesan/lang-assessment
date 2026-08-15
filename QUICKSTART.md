# Language Assessment System - Quick Start Guide

## Prerequisites

- Docker and Docker Compose
- Node.js 18+ (for local frontend development)
- Java 17+ (for local backend development)
- Maven 3.8+ (for building backend)
- PostgreSQL 15+ (if running without Docker)

## Option 1: Running with Docker Compose (Recommended)

### 1. Start All Services

```bash
cd /home/user/lang-assessment
docker compose up -d
```

This will start:
- PostgreSQL database (port 5432)
- Backend API (port 8080)
- Frontend (port 3000)
- MinIO S3 storage (port 9000)
- Flask evaluation service (port 5000)

### 2. Verify Services Are Running

```bash
docker compose ps
```

Expected output:
```
NAME                                    STATUS
lang-assessment-backend                 Up
lang-assessment-frontend                Up
lang-assessment-postgres                Up
lang-assessment-minio                   Up
lang-assessment-evaluation-service      Up
```

### 3. Access the Application

- **Admin Dashboard**: http://localhost:3000/login
- **Swagger API Docs**: http://localhost:8080/swagger-ui.html
- **MinIO Console**: http://localhost:9000

---

## Option 2: Running Locally (Development Mode)

### Backend Setup

```bash
# Navigate to backend directory
cd backend

# Build the project
mvn clean package -DskipTests

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/lang-assessment-backend-1.0.0.jar
```

Backend will be available at: `http://localhost:8080`

### Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm start

# Application opens at http://localhost:3000
```

### Database Setup (Without Docker)

```bash
# Create PostgreSQL database
createdb lang_assessment

# Update application.properties with your DB credentials
# backend/src/main/resources/application.properties

# Run backend (Hibernate will create schema automatically)
mvn spring-boot:run
```

---

## Testing Phase 1 & 2 Workflows

### Admin Login

1. Open http://localhost:3000/login
2. Enter credentials:
   - **Email**: `admin@langassessment.com`
   - **Password**: `password`

3. Click "Login"

### Phase 1.1: Assessment Setup

1. Navigate to **Admin Dashboard** → **Assessments**
2. Click **"New Assessment"** button
3. Fill in form:
   - **Title**: "English Proficiency Test"
   - **Description**: "Comprehensive English language assessment"
   - **Language**: Select from dropdown
4. Click **"Create Assessment"**
5. Expand created assessment card
6. Click **"Add Module"** button
7. Configure module:
   - **Module Type**: Select from LISTENING, READING, etc.
   - **Number of Questions**: 10
   - **Difficulty Level**: INTERMEDIATE
   - **Enabled**: Check
8. Click **"Add Module"**
9. Verify module appears in list

### Phase 1.2: Assessment Monitoring

1. Navigate to **Admin Dashboard** → **Monitoring**
2. Select an assessment from dropdown
3. View statistics cards:
   - Total Candidates
   - Invited
   - Started
   - Completed
   - Evaluated
4. View candidate progress table
5. Click **"Refresh"** to update data
6. Test **"Resend"** button for INVITED candidates

### Phase 2: Candidate Onboarding

1. Navigate to **Admin Dashboard** → **Onboarding**
2. Select assessment from dropdown
3. Try different candidate addition methods:

#### Method 1: Add Individual Candidate
- Click **"Add Candidate"** button
- Enter name: "John Doe"
- Enter email: "john@example.com"
- Click **"Add Candidate"**
- Verify candidate appears in list

#### Method 2: Bulk Upload
- Click **"Bulk Upload"** button
- Enter CSV data:
  ```
  Jane Smith, jane@example.com
  Bob Johnson, bob@example.com
  Alice Williams, alice@example.com
  ```
- Click **"Upload Candidates"**
- Verify all candidates added to list

#### Method 3: Download CSV
- Click **"Download CSV"** button
- Verify file contains emails and secure links

### Copy Secure Links

1. In candidate list, click **"Copy"** button
2. Verify "Copied!" message appears
3. Paste secure link to verify it's a full URL:
   ```
   http://localhost:3000/candidate-login?link=<uuid>
   ```

---

## API Testing with Curl

### Admin Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@langassessment.com",
    "password": "password"
  }'
```

Response includes `token` - use this in Authorization header for subsequent requests.

### List Assessments

```bash
curl http://localhost:8080/api/assessments?page=0&size=10 \
  -H "Authorization: Bearer {token}"
```

### Create Assessment

```bash
curl -X POST http://localhost:8080/api/assessments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "title": "Spanish Test",
    "description": "Spanish language assessment",
    "languageId": 1,
    "status": "DRAFT"
  }'
```

### Add Candidate

```bash
curl -X POST http://localhost:8080/api/admin/assessments/1/candidates \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com"
  }'
```

### Bulk Add Candidates

```bash
curl -X POST http://localhost:8080/api/admin/assessments/1/candidates/bulk-upload \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '[
    {"name": "Jane Smith", "email": "jane@example.com"},
    {"name": "Bob Johnson", "email": "bob@example.com"}
  ]'
```

### List Candidates

```bash
curl http://localhost:8080/api/admin/assessments/1/candidates?page=0&size=20 \
  -H "Authorization: Bearer {token}"
```

### Resend Invitation

```bash
curl -X POST http://localhost:8080/api/admin/candidates/1/resend-invitation \
  -H "Authorization: Bearer {token}"
```

---

## Database Exploration

### Connect to PostgreSQL

```bash
# Using Docker
docker exec -it lang-assessment-postgres psql -U postgres -d lang_assessment

# Or with psql if running locally
psql -h localhost -U postgres -d lang_assessment
```

### Useful Queries

```sql
-- List all users
SELECT * FROM users;

-- List all assessments
SELECT * FROM assessments;

-- List all candidates for assessment
SELECT u.name, u.email, ac.status, ac.secure_link
FROM assessment_candidates ac
JOIN users u ON ac.user_id = u.id
WHERE ac.assessment_id = 1;

-- List all modules
SELECT * FROM assessment_modules;

-- Count candidates by status
SELECT status, COUNT(*) as count
FROM assessment_candidates
GROUP BY status;
```

---

## Troubleshooting

### Backend Won't Start

**Error**: `Port 8080 already in use`
```bash
# Find and kill process using port 8080
lsof -i :8080
kill -9 <PID>
```

**Error**: `Database connection refused`
- Ensure PostgreSQL is running
- Check database credentials in `application.properties`
- Verify database exists: `createdb lang_assessment`

### Frontend Won't Load

**Error**: `Cannot reach API`
- Verify backend is running: `curl http://localhost:8080/health`
- Check CORS configuration
- Verify API URL in frontend axios config

**Error**: `npm install fails`
```bash
# Clear npm cache and reinstall
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### Login Fails

**Error**: `Invalid email or password`
- Verify admin user exists in database
- Check password hash: `SELECT * FROM users WHERE email='admin@langassessment.com';`
- Ensure JWT secret is configured correctly

### Docker Issues

**Check logs**:
```bash
docker compose logs backend
docker compose logs frontend
docker compose logs postgres
```

**Rebuild images**:
```bash
docker compose down
docker compose build --no-cache
docker compose up -d
```

---

## Performance Testing

### Load Testing API

```bash
# Using Apache Bench
ab -n 100 -c 10 http://localhost:8080/api/assessments?page=0&size=10

# Using curl in loop
for i in {1..10}; do
  curl http://localhost:8080/api/assessments?page=0&size=10
done
```

### Database Query Performance

```sql
-- Check assessment loading time
EXPLAIN ANALYZE
SELECT * FROM assessments WHERE id = 1;

-- Check candidate filtering performance
EXPLAIN ANALYZE
SELECT * FROM assessment_candidates
WHERE assessment_id = 1 AND status = 'INVITED';
```

---

## Next Steps

After validating Phase 1 & 2:

1. **Phase 3**: Implement candidate assessment taking
   - Create CandidateHome page
   - Build AssessmentTaking component
   - Connect to Flask evaluation service

2. **Testing**: Run full end-to-end candidate flow
   - Generate secure link in onboarding
   - Access assessment as candidate
   - Submit responses
   - View evaluation results

3. **Production**: Deploy to production environment
   - Update environment variables
   - Configure external services (Gemini API, email, etc.)
   - Set up monitoring and alerting
   - Perform security audit

---

## Useful Commands

```bash
# View backend logs
docker compose logs -f backend

# View frontend logs
docker compose logs -f frontend

# Stop all services
docker compose stop

# Restart a specific service
docker compose restart backend

# Remove all data (WARNING: destructive)
docker compose down -v

# Check service health
docker compose ps
curl http://localhost:8080/health
```

---

## Performance Tips

1. **Frontend**: Use React DevTools to identify re-render issues
2. **Backend**: Monitor database queries with Hibernate SQL logging
3. **Database**: Use indexes on frequently queried columns
4. **API**: Implement pagination for large result sets (already done)
5. **Caching**: Consider implementing Redis for session/token caching

---

## Security Checklist Before Production

- [ ] Update JWT secret to strong random value (64+ characters)
- [ ] Change admin password from default
- [ ] Enable HTTPS/TLS
- [ ] Set up rate limiting
- [ ] Enable CORS only for allowed domains
- [ ] Configure secure session cookies
- [ ] Set up WAF (Web Application Firewall)
- [ ] Implement audit logging
- [ ] Set up database backups
- [ ] Configure secrets management

---

**For detailed implementation status, see [IMPLEMENTATION_STATUS.md](./IMPLEMENTATION_STATUS.md)**
