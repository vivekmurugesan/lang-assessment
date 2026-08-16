# Language Assessment System - Setup Guide

Complete guide for setting up and configuring the Language Assessment System.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Quick Start](#quick-start)
3. [Configuration](#configuration)
4. [Email Setup](#email-setup)
5. [Database Setup](#database-setup)
6. [Troubleshooting](#troubleshooting)

## Prerequisites

### Required
- Docker & Docker Compose (for containerized deployment)
- OR Java 17+, Node.js 18+, Python 3.9+, PostgreSQL 14+ (for local development)

### Optional
- SMTP Server Access (for email invitations)
- Google Gemini API Key (for AI-powered evaluation)

## Quick Start

### Using Docker Compose

```bash
# Clone the repository
git clone https://github.com/vivekmurugesan/lang-assessment.git
cd lang-assessment

# Start all services
docker-compose up -d

# Access the application
# Frontend: http://localhost:3000
# Backend API: http://localhost:8080
# MinIO Console: http://localhost:9001
```

### Local Development

```bash
# Start Backend
cd backend
./mvnw spring-boot:run

# Start Frontend (new terminal)
cd frontend
npm install
npm start

# Start Evaluation Service (new terminal)
cd evaluation-service
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
python app.py
```

## Configuration

All configuration is managed through environment variables in `.env` file.

### Core Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | postgres | PostgreSQL host |
| `DB_PORT` | 5432 | PostgreSQL port |
| `DB_USER` | postgres | Database username |
| `DB_PASSWORD` | password123 | Database password ⚠️ Change in production |
| `JWT_SECRET` | (required) | JWT signing key (min 32 chars) |
| `JWT_EXPIRATION` | 86400000 | Token expiration (24 hours in ms) |

### MinIO Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `MINIO_ROOT_USER` | minioadmin | MinIO root user |
| `MINIO_ROOT_PASSWORD` | minioadmin123 | MinIO root password ⚠️ Change in production |
| `MINIO_BUCKET_ASSESSMENTS` | assessments | Bucket for assessment content |
| `MINIO_BUCKET_SUBMISSIONS` | submissions | Bucket for candidate submissions |

## Email Setup

### Why Email Configuration?

The system sends candidate invitation emails with:
- Assessment links
- Temporary passwords
- Assessment guidelines

Email is **optional** - the system works without it, but candidates won't receive automated invitations.

### Configuration Steps

#### 1. Gmail Setup (Recommended for Development)

```bash
# Edit .env file
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@langassessment.com
```

**Steps:**
1. Go to [Google Account Security](https://myaccount.google.com/security)
2. Enable 2-Step Verification
3. Go to [App Passwords](https://myaccount.google.com/apppasswords)
4. Select "Mail" and "Windows Computer" (or your device)
5. Copy the 16-character app password
6. Paste into `MAIL_PASSWORD` in `.env`

#### 2. Outlook Configuration

```bash
MAIL_HOST=smtp.office365.com
MAIL_PORT=587
MAIL_USERNAME=your-email@outlook.com
MAIL_PASSWORD=your-password
MAIL_FROM=noreply@langassessment.com
```

#### 3. SendGrid Configuration

```bash
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=SG.xxxxxxxxxxxx
MAIL_FROM=sender@example.com
```

#### 4. AWS SES Configuration

```bash
MAIL_HOST=email-smtp.us-east-1.amazonaws.com
MAIL_PORT=587
MAIL_USERNAME=your-ses-username
MAIL_PASSWORD=your-ses-password
MAIL_FROM=verified-sender@example.com
```

### Verifying Email Configuration

Once configured, send an invitation to a candidate:
1. Go to Admin Dashboard → Candidate Onboarding
2. Add a candidate with their email
3. Check the email inbox for the invitation

**If email not received:**
- Check backend logs for errors: `docker-compose logs backend | grep -i mail`
- Verify SMTP credentials are correct
- Check spam/junk folder
- Ensure sender email is verified with SMTP provider

## Database Setup

### Automatic Setup (Docker)

Database is automatically initialized when using Docker Compose:
- Tables created via Hibernate (`ddl-auto: update`)
- Initial data loaded from `database/seed.sql`

### Manual Setup (Local)

```bash
# Connect to PostgreSQL
psql -U postgres -h localhost

# Create database
CREATE DATABASE lang_assessment;

# Run initialization scripts
\c lang_assessment
\i database/init.sql
\i database/schema.sql
\i database/seed.sql
```

### Default Admin Credentials

After setup, admin login available with:
- **Email**: `admin@langassessment.com`
- **Password**: `password`

⚠️ **Change these credentials immediately in production!**

## Troubleshooting

### Backend Won't Start

**Error: "No qualifying bean of type 'JavaMailSender'"**
- Solution: Email configuration is optional. System will start without SMTP.
- Configure `MAIL_HOST` in `.env` to enable emails

**Error: "Connection refused" (Database)**
- Check PostgreSQL is running: `docker-compose ps`
- Verify `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD` in `.env`

### Email Not Sending

```bash
# Check backend logs
docker-compose logs backend | grep -i mail

# Verify credentials
# Test SMTP connection:
openssl s_client -connect smtp.gmail.com:587 -starttls smtp
```

### Frontend Not Loading

- Ensure backend is running: `http://localhost:8080/actuator/health`
- Check CORS configuration in `SecurityConfig.java`
- Clear browser cache: Ctrl+Shift+Delete

### Database Connection Issues

```bash
# Reset database (warning: data loss!)
docker-compose exec postgres psql -U postgres -d lang_assessment -c "DROP SCHEMA public CASCADE;"
docker-compose restart postgres
```

## Production Deployment

### Security Checklist

- [ ] Change `DB_PASSWORD` to strong password
- [ ] Change `MINIO_ROOT_PASSWORD` to strong password
- [ ] Change `JWT_SECRET` to random 64+ character string
- [ ] Change default admin credentials
- [ ] Configure email with production SMTP
- [ ] Set `LOG_LEVEL=WARN` in production
- [ ] Enable HTTPS/TLS
- [ ] Configure firewall rules
- [ ] Set up backup strategy for PostgreSQL

### Environment File for Production

```bash
# .env.production
DB_PASSWORD=<strong-random-password>
MINIO_ROOT_PASSWORD=<strong-random-password>
JWT_SECRET=<random-64-char-string>
MAIL_HOST=<production-smtp>
MAIL_USERNAME=<sender-email>
MAIL_PASSWORD=<smtp-password>
GEMINI_API_KEY=<your-api-key>
```

### Deployment

```bash
# Use production env file
cp .env.production .env

# Start with Docker Compose
docker-compose up -d

# Verify services
docker-compose ps
```

## Support

For issues or questions:
1. Check logs: `docker-compose logs -f`
2. Verify configuration in `.env`
3. Check GitHub Issues
4. Contact: [support@langassessment.com](mailto:support@langassessment.com)
