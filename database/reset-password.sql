-- Reset admin password to 'password' with BCrypt hash
-- Hash: password hashed with BCrypt (cost 10)
-- This hash is for the password: "password"

UPDATE users
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/KLm'
WHERE email = 'admin@langassessment.com';

-- Verify the update
SELECT id, email, name, role, is_active FROM users WHERE email = 'admin@langassessment.com';
