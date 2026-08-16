# Email Configuration Guide

Quick reference for setting up candidate invitation emails.

## Overview

The Language Assessment System can send automated emails to candidates with:
- Assessment assignment notifications
- Secure assessment links
- Temporary access passwords
- Assessment instructions

**Email is optional** - The system operates normally without SMTP configuration.

## Configuration Files

### 1. Environment File (`.env`)

Add these lines to `.env` in the project root:

```bash
# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@langassessment.com
```

### 2. Docker Compose (Auto-loaded)

If using Docker, email variables are automatically passed to the backend service from `.env` file.

### 3. Application Configuration

Backend uses Spring Mail properties from `application.yml`:

```yaml
mail:
  host: ${MAIL_HOST}
  port: ${MAIL_PORT}
  username: ${MAIL_USERNAME}
  password: ${MAIL_PASSWORD}
  from: ${MAIL_FROM}
  smtp:
    auth: true
    starttls:
      enable: true
      required: true
```

## Provider Setup Instructions

### Gmail (App Password Method)

**Best for**: Development, small deployments

1. Enable 2-Step Verification:
   - Go to [Google Account](https://myaccount.google.com/)
   - Click "Security" in left menu
   - Enable "2-Step Verification"

2. Generate App Password:
   - Go to [App Passwords](https://myaccount.google.com/apppasswords)
   - Select "Mail" and "Windows Computer" (or your device)
   - Click "Generate"
   - Copy the 16-character password

3. Configure `.env`:
   ```bash
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=xxxx xxxx xxxx xxxx
   MAIL_FROM=your-email@gmail.com
   ```

### Outlook / Office 365

1. Use your Outlook email credentials

2. Configure `.env`:
   ```bash
   MAIL_HOST=smtp.office365.com
   MAIL_PORT=587
   MAIL_USERNAME=your-email@outlook.com
   MAIL_PASSWORD=your-password
   MAIL_FROM=your-email@outlook.com
   ```

### SendGrid

**Best for**: Production, high volume

1. Create [SendGrid Account](https://sendgrid.com/)
2. Generate API Key
3. Create verified sender email address

4. Configure `.env`:
   ```bash
   MAIL_HOST=smtp.sendgrid.net
   MAIL_PORT=587
   MAIL_USERNAME=apikey
   MAIL_PASSWORD=SG.xxxxxxxxxxxxxxxxx
   MAIL_FROM=noreply@yourdomain.com
   ```

### AWS SES (Simple Email Service)

**Best for**: AWS deployments, high volume

1. Set up AWS SES in your region
2. Verify sender email address
3. Create SMTP credentials

4. Configure `.env`:
   ```bash
   MAIL_HOST=email-smtp.us-east-1.amazonaws.com
   MAIL_PORT=587
   MAIL_USERNAME=your-ses-username
   MAIL_PASSWORD=your-ses-password
   MAIL_FROM=verified-email@yourdomain.com
   ```

### Custom SMTP Server

Replace variables with your server details:

```bash
MAIL_HOST=your-smtp-server.com
MAIL_PORT=587
MAIL_USERNAME=your-username
MAIL_PASSWORD=your-password
MAIL_FROM=sender@yourdomain.com
```

## Testing Email Configuration

### Manual Test

1. **Start the backend with email configured:**
   ```bash
   docker-compose up -d
   ```

2. **Add a test candidate:**
   - Go to http://localhost:3000
   - Login as admin (admin@langassessment.com / password)
   - Navigate to Admin → Candidate Onboarding
   - Add new candidate with test email address

3. **Check email inbox:**
   - Email should arrive within 2-5 minutes
   - Check spam/junk folder
   - Verify sender is your configured `MAIL_FROM` address

### Debugging Email Issues

**Check backend logs:**
```bash
docker-compose logs backend | grep -i mail
```

**Common error messages:**
- `Authentication failed` → Wrong credentials
- `Connection refused` → Wrong host/port or firewall blocking
- `Certificate verification failed` → SSL/TLS issue

**Test SMTP Connection:**
```bash
telnet smtp.gmail.com 587
```

## Email Content

When a candidate is added, they receive:

**Subject:** `Language Assessment Invitation: [Assessment Name]`

**Body:**
```
Dear [Candidate Name],

You have been invited to take the '[Assessment Name]' language assessment.

Click the link below to begin:
http://localhost:3000/assessment/[secure-link]

The assessment evaluates your language proficiency according to the CEFR framework (A1-C2 levels).

If you have any questions, please contact the assessment administrator.

Best regards,
Language Assessment System
```

## Environment Variables Reference

| Variable | Required | Example | Notes |
|----------|----------|---------|-------|
| `MAIL_HOST` | No* | smtp.gmail.com | SMTP server hostname |
| `MAIL_PORT` | No* | 587 | SMTP port (usually 587 or 465) |
| `MAIL_USERNAME` | No* | user@gmail.com | SMTP authentication user |
| `MAIL_PASSWORD` | No* | xxxx xxxx xxxx | SMTP authentication password |
| `MAIL_FROM` | No* | noreply@example.com | Sender email address |

*Only required if you want to enable email notifications

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Email not sent, no errors in logs | Check `MAIL_HOST` is configured |
| "Connection refused" | Verify host/port, firewall may be blocking |
| "Authentication failed" | Check username/password, may need app-specific password |
| Emails in spam folder | Add to contacts, check spam filter rules |
| Gmail not working | Ensure App Password (not account password) is used |
| Port 587 not working | Try port 465 (SSL) instead |

## Security Notes

- **Never commit `.env` to git** - Add to `.gitignore`
- Use app-specific passwords (Gmail) instead of account passwords
- Rotate SMTP credentials regularly
- Use TLS/SSL encryption (port 587 or 465)
- Verify sender email with SMTP provider

## Disabling Email

If you need to disable email notifications:

1. Remove or comment out `MAIL_HOST` in `.env`
2. Backend will start normally, email sending will be skipped
3. Logs will show: "Email service not configured, skipping..."

## More Information

- [Spring Boot Mail Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.mail)
- [Gmail App Passwords](https://support.google.com/accounts/answer/185833)
- [SendGrid SMTP Integration](https://sendgrid.com/docs/for-developers/sending-email/integrating-with-the-smtp-api/)
- [AWS SES SMTP](https://docs.aws.amazon.com/ses/latest/dg/using-ses-with-your-application.html)
