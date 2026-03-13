-- H2 Database Initial Data
-- This file is automatically loaded when running with H2 profile
-- Data will be inserted if the table is empty

-- Insert admin user (from user's provided data)
INSERT INTO admin_model (id, email, full_name, mobile_no, password, role) 
SELECT 1, 'saurabh@gmail.com', 'saurabh borkar', '1234567890', '123456789', 'admin'
WHERE NOT EXISTS (SELECT 1 FROM admin_model WHERE email = 'saurabh@gmail.com');

-- Add more admin users as needed
-- INSERT INTO admin_model (email, full_name, mobile_no, password, role) 
-- VALUES ('admin@example.com', 'Admin User', '9876543210', 'password123', 'admin');

-- Sample student data (optional - uncomment if needed for testing)
-- INSERT INTO student_model (email, full_name, mobile_no, password, course, year, semister, college_name, dob, address, profile_img) 
-- VALUES ('student@example.com', 'Test Student', '1234567890', 'password123', 'Computer Science', '2024', '1', 'Test College', '2000-01-01', 'Test Address', NULL);
