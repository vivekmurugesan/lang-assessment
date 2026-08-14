# Language Assessment System - Deployment Guide

## Prerequisites

### System Requirements
- **OS**: Linux, macOS, or Windows (with Docker)
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Disk Space**: At least 10GB free
- **RAM**: Minimum 4GB (8GB recommended)

### Required Accounts/APIs
- **Google Gemini API Key**: For AI-powered evaluation
- **Email Service** (Optional): For candidate notifications

## Quick Start with Docker Compose

### 1. Environment Configuration

Update the `.env` file with your configuration:

```bash
# Database
DB_HOST=postgres
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=your-strong-password

# MinIO
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=your-strong-password

# JWT
JWT_SECRET=your-very-long-secret-key-minimum-32-characters-required

# AI Models
GEMINI_API_KEY=your-gemini-api-key-here
```

### 2. Start All Services

```bash
# Navigate to project directory
cd lang-assessment

# Build and start all containers
docker-compose up -d

# Verify all services are running
docker-compose ps
```

### 3. Access Applications

| Service | URL | Credentials |
|---------|-----|-------------|
| Frontend | http://localhost:3000 | - |
| Backend API | http://localhost:8080 | - |
| API Docs | http://localhost:8080/swagger-ui.html | - |
| MinIO Console | http://localhost:9001 | minioadmin / minioadmin123 |
| PostgreSQL | localhost:5432 | postgres / password123 |

## Development Setup

### Backend Development

```bash
# Navigate to backend directory
cd backend

# Install Maven (if not installed)
# macOS: brew install maven
# Ubuntu: sudo apt-get install maven

# Build the project
./mvnw clean install

# Run with debug enabled
./mvnw spring-boot:run -Dspring-boot.run.arguments="--debug"
```

### Frontend Development

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm start

# Build for production
npm run build
```

### Evaluation Service Development

```bash
# Navigate to evaluation service directory
cd evaluation-service

# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Run the service
python app.py
```

## Database Initialization

The database is automatically initialized with schema and seed data when PostgreSQL starts via Docker Compose. To manually initialize:

```bash
# Connect to PostgreSQL
docker exec -it lang-assessment-postgres psql -U postgres -d lang_assessment

# Check tables
\dt

# Run schema file manually if needed
\i /docker-entrypoint-initdb.d/02-schema.sql
```

## Monitoring and Logs

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f evaluation-service
docker-compose logs -f postgres
docker-compose logs -f minio
```

### Health Checks

```bash
# Backend health
curl http://localhost:8080/actuator/health

# Evaluation service health
curl http://localhost:5000/health

# MinIO health
curl http://localhost:9000/minio/health/live
```

## Production Deployment

### 1. Environment Configuration

Update `.env` for production:

```env
# Use strong passwords
DB_PASSWORD=generate-strong-password-here
MINIO_ROOT_PASSWORD=generate-strong-password-here

# Use secure JWT secret (minimum 32 characters)
JWT_SECRET=generate-secure-jwt-secret-minimum-32-characters

# API Configuration
REACT_APP_API_URL=https://your-domain.com/api
REACT_APP_WS_URL=wss://your-domain.com/ws

# Security
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=https://your-auth-provider

# Enable HTTPS
ENABLE_HTTPS=true
SSL_CERT_PATH=/path/to/cert.pem
SSL_KEY_PATH=/path/to/key.pem
```

### 2. Docker Compose Modifications

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  # ... (copy services from docker-compose.yml)
  
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - /etc/letsencrypt:/etc/letsencrypt
    depends_on:
      - frontend
      - backend
```

### 3. Scaling Configuration

For high-traffic deployments:

```yaml
backend:
  deploy:
    replicas: 3
  
frontend:
  deploy:
    replicas: 2
  
evaluation-service:
  deploy:
    replicas: 2
```

### 4. Backup Strategy

```bash
# Database backup
docker exec lang-assessment-postgres pg_dump -U postgres lang_assessment > backup.sql

# MinIO backup
docker exec lang-assessment-minio mc mirror minio/assessments ./local-backup/

# Automated daily backup script
#!/bin/bash
BACKUP_DIR="/backups/lang-assessment"
DATE=$(date +%Y%m%d)
docker exec lang-assessment-postgres pg_dump -U postgres lang_assessment > $BACKUP_DIR/db_$DATE.sql
```

## Security Checklist

- [ ] Change default passwords for all services
- [ ] Generate strong JWT secret (32+ characters)
- [ ] Configure SSL/TLS certificates
- [ ] Set up CORS with specific allowed origins
- [ ] Enable database encryption at rest
- [ ] Configure firewall rules
- [ ] Set up API rate limiting
- [ ] Enable audit logging
- [ ] Regular security updates for base images
- [ ] Use secrets management service (AWS Secrets Manager, HashiCorp Vault)

## Performance Tuning

### Database Optimization

```sql
-- Create indexes for frequently queried columns
CREATE INDEX idx_candidates_status ON assessment_candidates(status);
CREATE INDEX idx_submissions_created ON submissions(created_at);
CREATE INDEX idx_evaluation_results_level ON evaluation_results(cefr_level);

-- Analyze query performance
EXPLAIN ANALYZE SELECT * FROM submissions WHERE assessment_candidate_id = 1;
```

### Caching Strategy

Configure Redis for session and query caching:

```yml
redis:
  image: redis:7-alpine
  ports:
    - "6379:6379"
  volumes:
    - redis_data:/data
  command: redis-server --appendonly yes
```

### Connection Pooling

Backend database connection pool (in application.yml):

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
```

## Troubleshooting

### Common Issues

#### 1. Port Already in Use

```bash
# Find process using port
lsof -i :8080

# Kill process
kill -9 <PID>
```

#### 2. Database Connection Failed

```bash
# Check PostgreSQL status
docker exec lang-assessment-postgres pg_isready

# View PostgreSQL logs
docker logs lang-assessment-postgres
```

#### 3. MinIO Bucket Not Found

```bash
# Access MinIO
docker exec lang-assessment-minio mc ls minio/

# Create bucket manually
docker exec lang-assessment-minio mc mb minio/assessments
docker exec lang-assessment-minio mc mb minio/submissions
```

#### 4. Evaluation Service Timeout

```bash
# Increase timeout in docker-compose.yml
evaluation:
  environment:
    EVALUATION_SERVICE_TIMEOUT: 60
```

## Monitoring and Analytics

### Prometheus Metrics

Add Prometheus for monitoring:

```yaml
prometheus:
  image: prom/prometheus:latest
  volumes:
    - ./prometheus.yml:/etc/prometheus/prometheus.yml
  ports:
    - "9090:9090"
```

### Log Aggregation

Use ELK Stack for centralized logging:

```yaml
elasticsearch:
  image: docker.elastic.co/elasticsearch/elasticsearch:8.0.0
  environment:
    - discovery.type=single-node

kibana:
  image: docker.elastic.co/kibana/kibana:8.0.0
  ports:
    - "5601:5601"
```

## Maintenance

### Regular Tasks

- **Weekly**: Review audit logs and error rates
- **Monthly**: Database maintenance (VACUUM ANALYZE)
- **Quarterly**: Security patches and dependency updates
- **Annually**: Capacity planning and architecture review

### Update Procedure

```bash
# Pull latest images
docker-compose pull

# Stop existing services
docker-compose down

# Start with new images
docker-compose up -d

# Verify all services
docker-compose ps
```

## Support and Documentation

- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Database Schema**: See `database/schema.sql`
- **Environment Variables**: See `.env` file
- **Architecture**: See `docs/ARCHITECTURE.md`

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [MinIO Documentation](https://min.io/docs/)
- [Docker Documentation](https://docs.docker.com/)
