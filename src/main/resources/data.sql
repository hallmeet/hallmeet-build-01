-- MySQL (default profile) initial data
-- Runs once after schema creation when using MySQL. Idempotent: safe to run on every startup.

-- Insert default admin user if not present
INSERT INTO admin_model (email, full_name, mobile_no, password, role, deleted)
SELECT 'swapnil@gmail.com', 'swapnil', '1234567890', '123456789', 'admin', false
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM admin_model WHERE email = 'swapnil@gmail.com');
