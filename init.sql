-- Create the database if it doesn’t exist
CREATE DATABASE IF NOT EXISTS product_db;

-- Make sure the admin user exists and has full privileges
CREATE USER IF NOT EXISTS 'admin'@'%' IDENTIFIED BY 'admin123';
GRANT ALL PRIVILEGES ON *.* TO 'admin'@'%' WITH GRANT OPTION;

-- Apply changes
FLUSH PRIVILEGES;
