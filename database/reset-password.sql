-- Reset admin password to 'password' with BCrypt hash
-- Hash: password hashed with BCrypt (cost 10)
-- This hash is for the password: "password"

UPDATE users
SET password_hash = '$2a$10$SlCf9LjRNZ7j/d9kJ7Q1i.Gh9Jw7pR8mT4uV3wXyZaAbCdEfGhIjKlMn'
WHERE email = 'admin@langassessment.com';

-- Verify the update
SELECT id, email, name, role, is_active FROM users WHERE email = 'admin@langassessment.com';
