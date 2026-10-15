-- ==============================================================================
-- SMART CAMPUS RESOURCE ALLOCATION AND MONITORING SYSTEM
-- Database Schema for MySQL 5.7 / 8.0+
-- Database: smart_campus_db
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS smart_campus_db;
USE smart_campus_db;

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'FACULTY', -- 'ADMIN', 'FACULTY', 'STUDENT_COORDINATOR'
    department VARCHAR(100),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. CAMPUS RESOURCES TABLE
CREATE TABLE IF NOT EXISTS resources (
    resource_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(50) NOT NULL, -- 'SMART_CLASSROOM', 'LABORATORY', 'SEMINAR_HALL', 'EQUIPMENT'
    building VARCHAR(80) NOT NULL,
    floor_room VARCHAR(40) NOT NULL,
    capacity INT NOT NULL DEFAULT 1,
    power_rating_kw DOUBLE NOT NULL DEFAULT 2.0,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE', -- 'AVAILABLE', 'ALLOCATED', 'MAINTENANCE'
    features TEXT
);

-- 3. RESOURCE ALLOCATIONS TABLE
CREATE TABLE IF NOT EXISTS allocations (
    allocation_id INT AUTO_INCREMENT PRIMARY KEY,
    resource_id INT NOT NULL,
    user_id INT NOT NULL,
    purpose VARCHAR(255) NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'APPROVED', -- 'APPROVED', 'ACTIVE', 'COMPLETED', 'CANCELLED'
    allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 4. IOT TELEMETRY & MONITORING LOGS TABLE
CREATE TABLE IF NOT EXISTS telemetry_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    resource_id INT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    occupancy_count INT NOT NULL DEFAULT 0,
    power_draw_kw DOUBLE NOT NULL DEFAULT 0.0,
    temperature_c DOUBLE NOT NULL DEFAULT 24.0,
    hvac_status VARCHAR(20) DEFAULT 'OFF', -- 'ON', 'OFF', 'ECO'
    light_status VARCHAR(20) DEFAULT 'OFF', -- 'ON', 'OFF', 'DIM'
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE
);

-- 5. MAINTENANCE ALERTS TABLE
CREATE TABLE IF NOT EXISTS maintenance_alerts (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    resource_id INT NOT NULL,
    issue VARCHAR(255) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', -- 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    reported_date DATE NOT NULL,
    is_resolved BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE
);

-- ==============================================================================
-- INITIAL SEED DATA
-- ==============================================================================

-- Default Users (Password format can be plaintext for demo or hashed)
INSERT INTO users (username, password, full_name, role, department, email) VALUES
('admin', 'admin123', 'Campus Facility Director', 'ADMIN', 'Campus Administration', 'admin@smartcampus.edu'),
('sharma', 'sharma123', 'Dr. Rajesh Sharma', 'FACULTY', 'Computer Science & Engg', 'sharma@smartcampus.edu'),
('ananya', 'ananya123', 'Prof. Ananya Roy', 'FACULTY', 'Electronics & Comm', 'ananya@smartcampus.edu'),
('lucky', 'lucky123', 'Lucky Biswal (Student Lead)', 'STUDENT_COORDINATOR', 'Information Technology', 'lucky@smartcampus.edu')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- Default Campus Resources
INSERT INTO resources (resource_id, name, category, building, floor_room, capacity, power_rating_kw, status, features) VALUES
(1, 'APJ Abdul Kalam Seminar Hall', 'SEMINAR_HALL', 'Main Admin Block', 'Ground Floor (Auditorium 1)', 350, 12.5, 'AVAILABLE', 'Dolby Audio, Dual 4K Projectors, Automated Stage Lights, Central HVAC'),
(2, 'AI & Robotics Research Lab', 'LABORATORY', 'Tech Tower C', '3rd Floor - Lab 304', 60, 15.0, 'ALLOCATED', '30x RTX GPU Workstations, Robot Arm testbeds, High-speed fiber network'),
(3, 'Smart Lecture Theatre 101', 'SMART_CLASSROOM', 'Science Block A', '1st Floor - LT-101', 120, 4.5, 'AVAILABLE', 'Interactive Digital Whiteboard, Lecture Capture Camera, Wireless Microphones'),
(4, 'IoT & Embedded Systems Lab', 'LABORATORY', 'Electronics Wing', '2nd Floor - Lab 208', 45, 6.2, 'AVAILABLE', 'Oscilloscopes, Sensor Dev Kits, Soldering Stations, 3D Printers'),
(5, 'Cloud Computing & Cyber Lab', 'LABORATORY', 'Tech Tower C', '4th Floor - Lab 402', 75, 11.0, 'AVAILABLE', 'Dual Xeon Servers, Virtualization Cluster, Gigabit Ethernet'),
(6, 'Smart Classroom 205', 'SMART_CLASSROOM', 'Academic Block B', '2nd Floor - CR-205', 80, 3.2, 'AVAILABLE', 'Short-throw Projector, Acoustic ceiling, Motorized Blinds'),
(7, 'High-End 4K Mobile Projector Unit', 'EQUIPMENT', 'Media Center', 'AV Depot - Slot 4', 1, 0.6, 'AVAILABLE', '4000 Lumens, Wireless HDMI, Battery Backup'),
(8, 'EV Campus Shuttle Fast Charger 1', 'EQUIPMENT', 'East Parking Bay', 'Bay Station 1', 2, 22.0, 'AVAILABLE', 'Type-2 Fast AC Charging, RFID Authentication')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Default Sample Allocations
INSERT INTO allocations (resource_id, user_id, purpose, booking_date, start_time, end_time, status) VALUES
(2, 2, 'Hands-on Deep Learning Workshop', CURDATE(), '09:00:00', '13:00:00', 'ACTIVE'),
(1, 1, 'Annual Tech Fest Inauguration & Keynote', CURDATE(), '14:00:00', '17:30:00', 'APPROVED'),
(3, 3, 'Signals & Systems Guest Lecture', CURDATE(), '10:30:00', '12:30:00', 'APPROVED')
ON DUPLICATE KEY UPDATE purpose=VALUES(purpose);

-- Default Telemetry Logs
INSERT INTO telemetry_logs (resource_id, occupancy_count, power_draw_kw, temperature_c, hvac_status, light_status) VALUES
(1, 0, 0.5, 25.0, 'OFF', 'OFF'),
(2, 48, 13.8, 21.5, 'ON', 'ON'),
(3, 0, 0.2, 26.0, 'OFF', 'OFF'),
(4, 12, 3.1, 23.5, 'ECO', 'ON'),
(5, 0, 0.8, 24.0, 'OFF', 'OFF');

-- Default Maintenance Alerts
INSERT INTO maintenance_alerts (resource_id, issue, severity, reported_date, is_resolved) VALUES
(6, 'Projector HDMI port damaged by loose cable', 'MEDIUM', CURDATE(), FALSE),
(8, 'Charger cable insulation check recommended', 'LOW', CURDATE(), FALSE);
