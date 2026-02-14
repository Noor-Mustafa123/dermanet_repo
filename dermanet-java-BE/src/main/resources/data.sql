-- Create admin user (password: admin123)
-- Password is hashed using BCrypt
INSERT INTO users (name, email, password, role, created_at, updated_at)
VALUES ('Admin User', 'admin@dermanet.com', '$2a$10$XQjhZ5Z5Z5Z5Z5Z5Z5Z5ZuXQjhZ5Z5Z5Z5Z5Z5Z5ZuXQjhZ5Z5Z5Z', 'ADMIN', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Note: You need to hash the password using BCrypt before inserting
-- Use this Java code to generate the hash:
-- BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
-- String hashedPassword = encoder.encode("admin123");
