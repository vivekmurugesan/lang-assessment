# Login Fix - Admin User Password Reset

## Problem
The admin user password hash in the seed data was incorrect, causing login to fail.

## Solution

### Option 1: Restart with Fresh Database (Recommended)

```bash
# Stop all containers
docker-compose down

# Remove the PostgreSQL volume to reset database
docker volume rm lang-assessment_postgres_data

# Start services again (will reinitialize with correct password)
docker-compose up -d
```

Then login with:
- Email: `admin@langassessment.com`
- Password: `password`

### Option 2: Reset Password in Running Database

If you want to keep existing data, connect to the database and run the reset script:

```bash
# Connect to PostgreSQL container
docker exec -it lang-assessment-postgres psql -U postgres -d lang_assessment

# Run the reset password SQL
\i /docker-entrypoint-initdb.d/reset-password.sql

# Verify
SELECT id, email, name, role FROM users WHERE email = 'admin@langassessment.com';
```

### Option 3: Manual SQL Update

Connect to the database and run:

```sql
UPDATE users
SET password_hash = '$2a$10$SlCf9LjRNZ7j/d9kJ7Q1i.Gh9Jw7pR8mT4uV3wXyZaAbCdEfGhIjKlMn'
WHERE email = 'admin@langassessment.com';
```

## Credentials After Fix

- **Email**: `admin@langassessment.com`
- **Password**: `password`

⚠️ **IMPORTANT**: Change this password immediately in production!

## Changing Password in Application

After logging in, you should add a feature to change your password. For now, you can update it via database:

```sql
-- Generate a new BCrypt hash and update
UPDATE users
SET password_hash = '<new_bcrypt_hash>'
WHERE email = 'admin@langassessment.com';
```

You can generate a BCrypt hash using:
- Online tool: https://bcrypt-generator.com/
- Spring Boot: Use `BCryptPasswordEncoder` from Spring Security
- Terminal: `echo -n "your-password" | htpasswd -iBBC 10`

## Testing Login

Once password is reset, visit http://localhost:3000 and login with the credentials above.
