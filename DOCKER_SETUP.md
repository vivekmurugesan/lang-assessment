# Docker Compose Setup Guide

Complete setup guide for running the Language Assessment platform with Docker Compose.

## Prerequisites

- **Docker** (version 20.10+): https://docs.docker.com/get-docker/
- **Docker Compose** (version 2.0+): https://docs.docker.com/compose/install/
- **Git**

Verify installation:
```bash
docker --version
docker-compose --version
```

## Quick Start (3 Steps)

### Step 1: Clone and Navigate
```bash
cd ~/Code/Lang-Assessment-Claude/lang-assessment
```

### Step 2: Configure Environment
The `.env` file is already configured with defaults. For development, no changes needed.

**To customize:**
```bash
# Copy example file
cp .env.example .env

# Edit as needed
nano .env  # or your editor of choice
```

Key configurations in `.env`:
```bash
# Database
DB_NAME=lang_assessment
DB_USER=postgres
DB_PASSWORD=password123

# Text-to-Speech (Free - EdgeTTS)
TTS_PROVIDER=edge
EDGE_TTS_API_URL=http://edge-tts:5001/tts

# Gemini API (Optional - for question generation)
GEMINI_API_KEY=  # Leave empty for now, or set your API key
```

### Step 3: Start Everything
```bash
# Build and start all services
docker-compose -f docker-compose.dev.yml up -d

# Wait for services to be healthy (30-60 seconds)
docker-compose -f docker-compose.dev.yml ps

# Follow logs
docker-compose -f docker-compose.dev.yml logs -f backend
```

## Services Status

Once running, check service health:

```bash
# View all services
docker-compose -f docker-compose.dev.yml ps

# Expected output:
# NAME                 STATUS           PORTS
# lang-assessment-db   healthy (Up)     5432
# lang-assessment-minio healthy (Up)    9000, 9001
# lang-assessment-edge-tts healthy (Up) 5001
# lang-assessment-backend healthy (Up)  8080
```

## Access Services

| Service | URL | Purpose |
|---------|-----|---------|
| **Backend API** | http://localhost:8080 | REST API |
| **API Docs** | http://localhost:8080/swagger-ui.html | Swagger UI |
| **MinIO Console** | http://localhost:9001 | File storage UI |
| **EdgeTTS** | http://localhost:5001/health | Audio generation |
| **Database** | localhost:5432 | PostgreSQL |

## Frontend Setup

In a **separate terminal**:

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies (first time only)
npm install

# Start development server
npm start

# Runs on http://localhost:3000
```

## Configuration Options

### Use Different TTS Provider

**Option A: Google Cloud TTS** (requires API key + billing)
```bash
# In .env file:
TTS_PROVIDER=google
GOOGLE_TTS_API_KEY=AIza...your-key...
```

**Option B: ElevenLabs** (requires API key, better quality)
```bash
# In .env file:
TTS_PROVIDER=elevenlabs
ELEVENLABS_API_KEY=sk_...your-key...
ELEVENLABS_VOICE_ID=EXAVITQu4vr4xnSDxMaL
```

**Option C: EdgeTTS** (default - completely free)
```bash
# In .env file:
TTS_PROVIDER=edge
EDGE_TTS_API_URL=http://edge-tts:5001/tts
```

### Add Gemini API Key (for Question Generation)

1. Get API key from: https://makersuite.google.com/app/apikey
2. Update `.env`:
   ```bash
   GEMINI_API_KEY=your-api-key-here
   ```
3. Restart backend:
   ```bash
   docker-compose -f docker-compose.dev.yml restart backend
   ```

### Configure Mail (for Scorecard Sharing)

Update in `.env`:
```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password  # Use app password, not account password
MAIL_FROM=noreply@langassessment.com
```

## Common Commands

### View Logs
```bash
# All services
docker-compose -f docker-compose.dev.yml logs -f

# Specific service
docker-compose -f docker-compose.dev.yml logs -f backend
docker-compose -f docker-compose.dev.yml logs -f edge-tts

# Follow last 100 lines
docker-compose -f docker-compose.dev.yml logs -f --tail=100 backend
```

### Stop Services
```bash
# Stop all running services
docker-compose -f docker-compose.dev.yml stop

# Stop and remove containers
docker-compose -f docker-compose.dev.yml down

# Stop and remove everything including volumes (WARNING: deletes database!)
docker-compose -f docker-compose.dev.yml down -v
```

### Restart Services
```bash
# Restart all
docker-compose -f docker-compose.dev.yml restart

# Restart specific service
docker-compose -f docker-compose.dev.yml restart backend
docker-compose -f docker-compose.dev.yml restart edge-tts
```

### Rebuild Services
```bash
# Rebuild without cache
docker-compose -f docker-compose.dev.yml build --no-cache

# Rebuild and restart
docker-compose -f docker-compose.dev.yml build --no-cache && \
docker-compose -f docker-compose.dev.yml up -d
```

### Access Database
```bash
# Connect to PostgreSQL
docker-compose -f docker-compose.dev.yml exec postgres psql -U postgres -d lang_assessment

# Common queries inside psql:
# \dt                    - List all tables
# \d questions           - Describe questions table
# SELECT COUNT(*) FROM questions;
# \q                     - Exit psql
```

### Access MinIO Storage
1. Open http://localhost:9001
2. Login with:
   - Username: `minioadmin`
   - Password: `minioadmin123`
3. Navigate to `questions` bucket to see uploaded files

## Troubleshooting

### "Port already in use" error
```bash
# Find what's using the port
lsof -i :8080  # Backend
lsof -i :5432  # Database
lsof -i :9000  # MinIO
lsof -i :5001  # EdgeTTS

# Kill the process (if needed)
kill -9 <PID>

# Or use different ports in docker-compose.dev.yml
# Change "8080:8080" to "8081:8080" etc.
```

### "Invalid interpolation format" error
```
ERROR: Invalid interpolation format for "environment" option
```
**Solution:** Make sure `.env` file exists in project root
```bash
cp .env.example .env
docker-compose -f docker-compose.dev.yml up -d
```

### Services won't start
```bash
# Check service logs
docker-compose -f docker-compose.dev.yml logs backend

# Common issues:
# - Port in use: See above
# - Disk space: docker system prune
# - Memory: Increase Docker memory limit
# - Permissions: sudo docker-compose -f ...
```

### EdgeTTS audio generation fails
```bash
# Verify EdgeTTS is running
curl http://localhost:5001/health

# If not running, check logs
docker-compose -f docker-compose.dev.yml logs edge-tts

# Restart EdgeTTS
docker-compose -f docker-compose.dev.yml restart edge-tts
```

### Backend can't connect to database
```bash
# Check database is running
docker-compose -f docker-compose.dev.yml ps postgres

# Check database logs
docker-compose -f docker-compose.dev.yml logs postgres

# Verify connection
docker-compose -f docker-compose.dev.yml exec postgres psql -U postgres -c "SELECT 1"
```

### How to reset everything
```bash
# Remove all containers, networks, and volumes
docker-compose -f docker-compose.dev.yml down -v

# Remove images
docker rmi lang-assessment-backend lang-assessment-edge-tts minio postgres

# Start fresh
docker-compose -f docker-compose.dev.yml build --no-cache
docker-compose -f docker-compose.dev.yml up -d
```

## Development Workflow

### 1. Backend Code Changes
```bash
# Edit backend code in ./backend/...

# Rebuild and restart backend service
docker-compose -f docker-compose.dev.yml build --no-cache backend
docker-compose -f docker-compose.dev.yml up -d backend

# View logs
docker-compose -f docker-compose.dev.yml logs -f backend
```

### 2. Frontend Code Changes
```bash
# Frontend runs with hot reload (npm start)
# Changes are reflected automatically without restart
```

### 3. Database Schema Changes
```bash
# Place migration files in: backend/src/main/resources/db/migration/
# Flyway automatically runs migrations on startup
# No manual action needed

# Verify migrations
docker-compose -f docker-compose.dev.yml logs backend | grep "Flyway"
```

### 4. Configuration Changes
```bash
# Update .env file
nano .env

# Restart affected services
docker-compose -f docker-compose.dev.yml restart backend

# Or for TTS provider change
docker-compose -f docker-compose.dev.yml restart backend edge-tts
```

## Performance Tips

### Reduce Startup Time
```bash
# Build images only once during first setup
docker-compose -f docker-compose.dev.yml build

# Don't rebuild every time
docker-compose -f docker-compose.dev.yml up -d  # Reuses images
```

### Free Up Space
```bash
# Remove unused images
docker image prune

# Remove unused volumes
docker volume prune

# Remove everything unused
docker system prune -a
```

### View Resource Usage
```bash
# See CPU, memory usage
docker stats

# See container details
docker-compose -f docker-compose.dev.yml ps -a
```

## Production Considerations

⚠️ **This setup is for DEVELOPMENT ONLY**

For production, you should:

1. **Use environment-specific compose file**
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

2. **Security:**
   - Change all default passwords
   - Use strong JWT_SECRET
   - Enable HTTPS/TLS
   - Use environment variables for secrets (not in .env file)

3. **Scaling:**
   - Use container orchestration (Kubernetes, Docker Swarm)
   - Set resource limits
   - Use load balancer

4. **Monitoring:**
   - Add logging service (ELK, Datadog)
   - Add monitoring (Prometheus, Grafana)
   - Set up alerting

## Next Steps

1. ✅ Start services: `docker-compose -f docker-compose.dev.yml up -d`
2. ✅ Verify all healthy: `docker-compose -f docker-compose.dev.yml ps`
3. ✅ Start frontend: `cd frontend && npm start`
4. ✅ Open http://localhost:3000
5. ✅ Login and start using the application

## Support

For issues or questions:
1. Check troubleshooting section above
2. Review service logs: `docker-compose -f docker-compose.dev.yml logs service-name`
3. Check the project README for more information

## Additional Resources

- Docker Docs: https://docs.docker.com/
- Docker Compose: https://docs.docker.com/compose/
- PostgreSQL: https://www.postgresql.org/docs/
- MinIO: https://docs.min.io/
- EdgeTTS: https://github.com/rany2/edge-tts
