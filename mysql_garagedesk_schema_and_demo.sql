-- ===================================================================
-- GarageDesk: Vehicle Service Job Card and Bay Scheduling System
-- Sri Eshwar College of Engineering - Project Leap (Academic Year 2026-2027)
-- Assessment Question 67 | Student Register No: 060
-- Target Database: MySQL 8.0+ (MySQL Workbench Ready)
-- ===================================================================

CREATE DATABASE IF NOT EXISTS garagedesk_db;
USE garagedesk_db;

-- -------------------------------------------------------------------
-- 1. Table: VEHICLES
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    registration_number VARCHAR(30) NOT NULL UNIQUE,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    manufacturing_year INT,
    owner_name VARCHAR(100) NOT NULL,
    owner_phone VARCHAR(20) NOT NULL,
    owner_email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_vehicle_reg (registration_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------------
-- 2. Table: BAYS
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bays (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bay_number VARCHAR(50) NOT NULL UNIQUE,
    bay_type VARCHAR(100),
    status VARCHAR(25) NOT NULL DEFAULT 'AVAILABLE',
    INDEX idx_bay_number (bay_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------------
-- 3. Table: MECHANICS
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS mechanics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100),
    phone VARCHAR(20),
    status VARCHAR(25) NOT NULL DEFAULT 'AVAILABLE',
    hourly_rate DECIMAL(10, 2) DEFAULT 500.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------------
-- 4. Table: JOB_CARDS (Core Workflow Entity)
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS job_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    bay_id BIGINT,
    mechanic_id BIGINT,
    status VARCHAR(30) NOT NULL DEFAULT 'WAITING',
    requested_services VARCHAR(500),
    customer_complaints VARCHAR(500),
    quality_check_notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
    FOREIGN KEY (bay_id) REFERENCES bays(id) ON DELETE SET NULL,
    FOREIGN KEY (mechanic_id) REFERENCES mechanics(id) ON DELETE SET NULL,
    INDEX idx_job_status (status),
    INDEX idx_job_bay (bay_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------------
-- 5. Table: SERVICE_ITEMS (Parts Used & Labour Charges)
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS service_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_card_id BIGINT NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    item_type VARCHAR(25) NOT NULL DEFAULT 'PART', -- 'PART' or 'LABOUR_SERVICE'
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (job_card_id) REFERENCES job_cards(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------------
-- 6. Table: BILLS (Final Service Invoices)
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_number VARCHAR(50) NOT NULL UNIQUE,
    job_card_id BIGINT NOT NULL UNIQUE,
    parts_total DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    labour_charges DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    tax_rate DECIMAL(5, 2) NOT NULL DEFAULT 18.00, -- 18% GST
    tax_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    payment_status VARCHAR(25) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    billing_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP NULL,
    FOREIGN KEY (job_card_id) REFERENCES job_cards(id) ON DELETE CASCADE,
    INDEX idx_bill_number (bill_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------------
-- 7. Table: USERS (Authentication & Role-Based Access Control)
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    role VARCHAR(40) NOT NULL DEFAULT 'WORKSHOP_MANAGER',
    staff_badge_number VARCHAR(60),
    auth_provider VARCHAR(50) DEFAULT 'LOCAL',
    avatar_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_username (username),
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------------
-- 8. Table: AUDIT_LOGS (Accountability & Audit Trail)
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_name VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    performed_by VARCHAR(100) DEFAULT 'SYSTEM',
    details VARCHAR(1000),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_entity (entity_name, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===================================================================
-- SAMPLE DATA INSERTION (FOR IMMEDIATE LIVE TESTING IN MYSQL WORKBENCH)
-- ===================================================================

-- Clear existing data if re-running
DELETE FROM service_items;
DELETE FROM bills;
DELETE FROM job_cards;
DELETE FROM vehicles;
DELETE FROM bays;
DELETE FROM mechanics;
DELETE FROM users;
DELETE FROM audit_logs;

-- Seed Authenticated Users (Workshop Team & Clients)
INSERT INTO users (id, username, email, password, full_name, role, staff_badge_number, auth_provider) VALUES
(1, 'admin', 'admin@garagedesk.com', 'garage2026', 'Mahilesh', 'WORKSHOP_MANAGER', 'SECE-060', 'LOCAL'),
(2, 'client', 'client@garagedesk.com', 'client123', 'Priya Sharma', 'CLIENT', 'CLIENT-01', 'LOCAL'),
(3, 'advisor', 'advisor@garagedesk.com', 'advisor123', 'Arun Kumar', 'SERVICE_ADVISOR', 'ADV-01', 'LOCAL'),
(4, 'mechanic', 'mechanic@garagedesk.com', 'mech123', 'Rajesh Kumar', 'MECHANIC', 'MCH-01', 'LOCAL'),
(5, 'inspector', 'inspector@garagedesk.com', 'qc123', 'Suresh Babu', 'QUALITY_INSPECTOR', 'QC-01', 'LOCAL');

-- Seed Service Bays
INSERT INTO bays (id, bay_number, bay_type, status) VALUES
(1, 'Bay 1', 'Express Lube & Quick Service', 'OCCUPIED'),
(2, 'Bay 2', 'General Mechanical & Suspension', 'AVAILABLE'),
(3, 'Bay 3', 'Wheel Alignment & Balancing', 'AVAILABLE'),
(4, 'Bay 4', 'Computer Diagnostics & Electrical', 'AVAILABLE');

-- Seed Mechanics
INSERT INTO mechanics (id, name, specialization, phone, status, hourly_rate) VALUES
(1, 'Rajesh Kumar', 'Senior Engine Specialist', '9876543210', 'BUSY', 650.00),
(2, 'Suresh Babu', 'Brake & Suspension', '9876543211', 'AVAILABLE', 500.00),
(3, 'Anand Prakash', 'Diagnostics & Electrical', '9876543212', 'AVAILABLE', 550.00);

-- Seed Customer Vehicles
INSERT INTO vehicles (id, registration_number, brand, model, manufacturing_year, owner_name, owner_phone, owner_email) VALUES
(1, 'TN-38-BZ-4521', 'Toyota', 'Innova Crysta', 2021, 'Arun Kumar', '9842100001', 'arun@example.com'),
(2, 'TN-37-CK-9912', 'Honda', 'City ZX', 2022, 'Priya Sharma', '9842100002', 'priya@example.com'),
(3, 'TN-66-E-1004', 'Hyundai', 'Creta SX', 2020, 'Karthik Raja', '9842100003', 'karthik@example.com');

-- Seed Active Job Card in Bay 1
INSERT INTO job_cards (id, vehicle_id, bay_id, mechanic_id, status, requested_services, customer_complaints, created_at) VALUES
(1, 1, 1, 1, 'IN_PROGRESS', 'Engine oil flush, synthetic 5W-30 replacement, brake pad check', 'Squeaking sound when braking', NOW());

-- Seed Waiting Job Card
INSERT INTO job_cards (id, vehicle_id, bay_id, mechanic_id, status, requested_services, customer_complaints, created_at) VALUES
(2, 2, NULL, NULL, 'WAITING', 'Periodic 20,000 km Service & AC inspection', 'AC cooling is slightly low', NOW());

-- Seed Logged Parts & Labour on Job #1 and Job #2
INSERT INTO service_items (id, job_card_id, item_name, item_type, quantity, unit_price, total_price) VALUES
(1, 1, 'Castrol Edge 5W-30 Fully Synthetic Oil (4L)', 'PART', 1, 2850.00, 2850.00),
(2, 1, 'Genuine OEM Oil Filter', 'PART', 1, 450.00, 450.00),
(3, 1, 'General Service Labour & Inspection', 'LABOUR_SERVICE', 2, 650.00, 1300.00),
(4, 2, 'R134a AC Refrigerant Gas Recharge', 'PART', 1, 1450.00, 1450.00),
(5, 2, 'Honda OEM Carbon Cabin Air Filter', 'PART', 1, 650.00, 650.00),
(6, 2, 'AC Evaporator Flush & Diagnostic Labour', 'LABOUR_SERVICE', 1, 800.00, 800.00);

-- Seed Sample Audit Trail
INSERT INTO audit_logs (entity_name, entity_id, action, performed_by, details, timestamp) VALUES
('Vehicle', 1, 'REGISTER', 'SYSTEM', 'Registered vehicle TN-38-BZ-4521', NOW() - INTERVAL 2 HOUR),
('JobCard', 1, 'CREATE', 'SERVICE_ADVISOR', 'Created job card for TN-38-BZ-4521', NOW() - INTERVAL 1 HOUR),
('JobCard', 1, 'ASSIGN', 'SERVICE_ADVISOR', 'Assigned Job Card #1 to Bay 1 and Mechanic Rajesh Kumar', NOW() - INTERVAL 50 MINUTE),
('JobCard', 1, 'ADD_ITEM', 'MECHANIC', 'Added Castrol Edge 5W-30 and Oil Filter', NOW() - INTERVAL 30 MINUTE);

-- ===================================================================
-- EXAMINER DEMO QUERIES (RUN THESE IN MYSQL WORKBENCH DURING DEMO!)
-- ===================================================================

-- 🔍 DEMO QUERY 1: Show all service bays and their current occupancy status
SELECT 
    b.bay_number AS 'Bay Name',
    b.bay_type AS 'Bay Specialty',
    b.status AS 'Bay Status',
    COALESCE(j.id, 'None') AS 'Active Job #',
    COALESCE(v.registration_number, 'Empty') AS 'Vehicle in Bay',
    COALESCE(m.name, 'Unassigned') AS 'Assigned Mechanic'
FROM bays b
LEFT JOIN job_cards j ON b.id = j.bay_id AND j.status NOT IN ('COMPLETED', 'CANCELLED')
LEFT JOIN vehicles v ON j.vehicle_id = v.id
LEFT JOIN mechanics m ON j.mechanic_id = m.id
ORDER BY b.id;

-- 🔍 DEMO QUERY 2: Demonstrate Business Rule 1 (Bay Conflict Detection)
-- Find any bay that is currently occupied and cannot accept new jobs
SELECT 
    b.bay_number, 
    b.status, 
    j.id AS active_job_card_id, 
    j.status AS job_status, 
    v.registration_number
FROM bays b
JOIN job_cards j ON b.id = j.bay_id
JOIN vehicles v ON j.vehicle_id = v.id
WHERE j.status NOT IN ('COMPLETED', 'CANCELLED');

-- 🔍 DEMO QUERY 3: Detailed Job Card Progress with Vehicle and Owner details
SELECT 
    j.id AS 'Job Card ID',
    v.registration_number AS 'Vehicle Plate',
    CONCAT(v.brand, ' ', v.model) AS 'Vehicle Model',
    v.owner_name AS 'Customer Name',
    v.owner_phone AS 'Customer Phone',
    b.bay_number AS 'Service Bay',
    m.name AS 'Technician',
    j.status AS 'Current Workflow Stage',
    j.requested_services AS 'Work Order'
FROM job_cards j
JOIN vehicles v ON j.vehicle_id = v.id
LEFT JOIN bays b ON j.bay_id = b.id
LEFT JOIN mechanics m ON j.mechanic_id = m.id;

-- 🔍 DEMO QUERY 4: Feature 5 - Compute Parts, Labour, and 18% GST Breakdown for Job #1
SELECT 
    j.id AS job_card_id,
    SUM(CASE WHEN s.item_type = 'PART' THEN s.total_price ELSE 0 END) AS parts_subtotal,
    SUM(CASE WHEN s.item_type = 'LABOUR_SERVICE' THEN s.total_price ELSE 0 END) AS labour_subtotal,
    SUM(s.total_price) AS subtotal,
    ROUND(SUM(s.total_price) * 0.18, 2) AS gst_18_percent,
    ROUND(SUM(s.total_price) * 1.18, 2) AS total_payable_amount
FROM job_cards j
JOIN service_items s ON j.id = s.job_card_id
WHERE j.id = 1
GROUP BY j.id;

-- 🔍 DEMO QUERY 5: Show Accountability Audit Trail
SELECT 
    id,
    entity_name,
    entity_id,
    action,
    performed_by,
    details,
    DATE_FORMAT(timestamp, '%Y-%m-%d %H:%i:%s') AS formatted_time
FROM audit_logs
ORDER BY timestamp DESC;
