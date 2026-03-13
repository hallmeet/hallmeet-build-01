-- Initialize database schema
-- This script runs automatically when MySQL container starts for the first time

CREATE DATABASE IF NOT EXISTS hall_ticket;
USE hall_ticket;

-- Grant privileges to the user
GRANT ALL PRIVILEGES ON hall_ticket.* TO 'halluser'@'%';
FLUSH PRIVILEGES;




