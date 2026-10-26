-- =====================================================================
-- COMPLETE SAMPLE DATA SEED SCRIPT
-- ONLINE COURSE REGISTRATION MANAGEMENT SYSTEM
-- Target DBMS: MySQL 8.0+
-- Passwords:
--   Admin:   admin123   (BCrypt hash below)
--   Faculty: faculty123 (BCrypt hash below)
--   Student: 12345      (BCrypt hash below)
-- =====================================================================

USE online_course_registration;

-- Disable foreign key checks for clean sequential seeding
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- 1. Admin Record (admin@college.edu / admin123)
-- -----------------------------------------------------
TRUNCATE TABLE admin;
INSERT INTO admin (admin_id, name, email, password) VALUES
(1, 'System Administrator', 'admin@college.edu', '$2a$10$ADDEuXQrX3PuXdvSKR5EEezaU4uK4w/dm.8ZjK1KOPudCiE1TBAv6');

-- -----------------------------------------------------
-- 2. Degree Branches
-- -----------------------------------------------------
TRUNCATE TABLE branch;
INSERT INTO branch (id, branch_code, branch_name, active) VALUES
(1, 'AIML', 'Artificial Intelligence & Machine Learning', TRUE),
(2, 'CSE',  'Computer Science & Engineering',             TRUE),
(3, 'ECE',  'Electronics & Communication Engineering',    TRUE);

-- -----------------------------------------------------
-- 3. Minor Streams (Linked to Branches)
-- -----------------------------------------------------
TRUNCATE TABLE minor_stream;
INSERT INTO minor_stream (id, stream_code, stream_name, branch_id, active) VALUES
(1, 'MAD',  'AIML MAD Stream',                 1, TRUE),
(2, 'NA',   'AIML Network Analysis Stream',    1, TRUE),
(3, 'CA',   'AIML Conjugate Analysis Stream',  1, TRUE),
(4, 'WD',   'CSE Web Development Stream',      2, TRUE),
(5, 'CC',   'CSE Cloud Computing Stream',      2, TRUE),
(6, 'VLSI', 'ECE VLSI Design Stream',          3, TRUE);

-- -----------------------------------------------------
-- 4. Faculty Members (password: faculty123)
-- -----------------------------------------------------
TRUNCATE TABLE faculty;
INSERT INTO faculty (id, faculty_code, name, email, password, department, designation, phone, active) VALUES
(1, 'FAC001', 'Dr. Ravi Kumar',        'ravi.kumar@college.edu',      '$2a$10$RqwY8AXRr6IGJvb/MJ/HW.CgaCooPAOiQTFY5VSUD4TSwUthWY9C.', 'AIML', 'Professor',           '9876500001', TRUE),
(2, 'FAC002', 'Dr. Priya Sharma',      'priya.sharma@college.edu',    '$2a$10$RqwY8AXRr6IGJvb/MJ/HW.CgaCooPAOiQTFY5VSUD4TSwUthWY9C.', 'AIML', 'Associate Professor', '9876500002', TRUE),
(3, 'FAC003', 'Prof. Rajesh Verma',    'rajesh.verma@college.edu',    '$2a$10$RqwY8AXRr6IGJvb/MJ/HW.CgaCooPAOiQTFY5VSUD4TSwUthWY9C.', 'CSE',  'Assistant Professor', '9876500003', TRUE),
(4, 'FAC004', 'Dr. Ananya Sen',        'ananya.sen@college.edu',      '$2a$10$RqwY8AXRr6IGJvb/MJ/HW.CgaCooPAOiQTFY5VSUD4TSwUthWY9C.', 'ECE',  'Professor',           '9876500004', TRUE),
(5, 'FAC005', 'Prof. Vikram Malhotra', 'vikram.malhotra@college.edu', '$2a$10$RqwY8AXRr6IGJvb/MJ/HW.CgaCooPAOiQTFY5VSUD4TSwUthWY9C.', 'CSE',  'Associate Professor', '9876500005', TRUE);

-- -----------------------------------------------------
-- 5. Semesters
-- -----------------------------------------------------
TRUNCATE TABLE semester;
INSERT INTO semester (id, semester_number, academic_year, active) VALUES
(1, 3, '2025-26', TRUE),
(2, 4, '2025-26', TRUE),
(3, 5, '2025-26', TRUE),
(4, 6, '2025-26', TRUE),
(5, 7, '2025-26', TRUE),
(6, 8, '2025-26', TRUE);

-- -----------------------------------------------------
-- 6. Sections (Faculty Assigned at Section Level)
-- -----------------------------------------------------
TRUNCATE TABLE section;
INSERT INTO section (id, section_name, branch_id, minor_stream_id, academic_year, faculty_id, active) VALUES
(1, 'MAD Stream-1', 1, 1, '2025-26', 1, TRUE),
(2, 'MAD Stream-2', 1, 1, '2025-26', 2, TRUE),
(7, 'MAD Stream-3', 1, 1, '2025-26', 1, TRUE),
(3, 'NA Stream-1',  1, 2, '2025-26', 1, TRUE),
(4, 'CA Stream-1',  1, 3, '2025-26', 2, TRUE),
(5, 'WD Stream-1',  2, 4, '2025-26', 3, TRUE),
(6, 'VLSI Stream-1', 3, 6, '2025-26', 4, TRUE);

-- -----------------------------------------------------
-- 7. Courses (With Branch, Minor Stream, Semester & Version)
-- -----------------------------------------------------
TRUNCATE TABLE course;
INSERT INTO course (course_id, course_code, course_name, description, department, credits, faculty_name, max_seats, available_seats, duration, status, branch_id, minor_stream_id, semester_id, faculty_id, version) VALUES
(1,  'MAD301', 'Mobile App Development Fundamentals', 'Android and iOS app development basics, UI design, lifecycle management.', 'AIML', 4, 'Dr. Ravi Kumar',        30, 29, '14 Weeks', 'ACTIVE', 1, 1, 1, 1, 0),
(2,  'MAD501', 'Advanced Mobile Development',          'Advanced React Native, Flutter, state management, app deployment.',         'AIML', 4, 'Dr. Priya Sharma',      30, 30, '14 Weeks', 'ACTIVE', 1, 1, 3, 2, 0),
(3,  'NA301',  'Network Analysis Fundamentals',        'Graph theory, network topology, flow analysis, bandwidth optimization.',    'AIML', 3, 'Dr. Ravi Kumar',        25, 25, '12 Weeks', 'ACTIVE', 1, 2, 1, 1, 0),
(4,  'NA501',  'Advanced Network Analysis',            'Deep packet inspection, network forensics, protocol analysis.',             'AIML', 3, 'Dr. Ravi Kumar',        25, 24, '12 Weeks', 'ACTIVE', 1, 2, 3, 1, 0),
(5,  'CA301',  'Conjugate Analysis Basics',            'Mathematical foundations for conjugate systems, signal processing basics.', 'AIML', 3, 'Dr. Priya Sharma',      25, 25, '12 Weeks', 'ACTIVE', 1, 3, 1, 2, 0),
(6,  'CA501',  'Advanced Conjugate Systems',            'Advanced signal decomposition, conjugate transform applications.',         'AIML', 3, 'Dr. Priya Sharma',      25, 25, '12 Weeks', 'ACTIVE', 1, 3, 3, 2, 0),
(7,  'WD301',  'Web Development Fundamentals',         'HTML5, CSS3, JavaScript ES6+, responsive design, REST APIs.',               'CSE',  4, 'Prof. Vikram Malhotra', 35, 34, '12 Weeks', 'ACTIVE', 2, 4, 1, 5, 0),
(8,  'WD501',  'Full Stack Development',               'React.js, Spring Boot, microservices, Docker deployment.',                  'CSE',  4, 'Prof. Rajesh Verma',    35, 35, '14 Weeks', 'ACTIVE', 2, 4, 3, 3, 0),
(9,  'CC301',  'Cloud Computing Fundamentals',         'AWS basics, Azure fundamentals, IaaS, PaaS, SaaS, virtualization.',         'CSE',  3, 'Prof. Rajesh Verma',    30, 30, '10 Weeks', 'ACTIVE', 2, 5, 1, 3, 0),
(10, 'CC501',  'Advanced Cloud Architecture',          'Serverless, Kubernetes, cloud security, multi-cloud strategies.',           'CSE',  3, 'Prof. Vikram Malhotra', 30, 30, '12 Weeks', 'ACTIVE', 2, 5, 3, 5, 0),
(11, 'VL301',  'VLSI Design Fundamentals',             'CMOS design, digital circuits, FPGA programming basics.',                   'ECE',  4, 'Dr. Ananya Sen',        20, 20, '14 Weeks', 'ACTIVE', 3, 6, 1, 4, 0),
(12, 'VL501',  'Advanced VLSI Architecture',           'ASIC design, chip layout, low-power design techniques.',                    'ECE',  4, 'Dr. Ananya Sen',        20, 20, '14 Weeks', 'ACTIVE', 3, 6, 3, 4, 0);

-- -----------------------------------------------------
-- 8. Students (Fixed Minor Stream from 2nd Year, password: 12345)
-- -----------------------------------------------------
TRUNCATE TABLE student;
INSERT INTO student (student_id, name, email, password, roll_no, department, academic_year, phone, branch_id, minor_stream_id, section_id, semester_id, status) VALUES
(1, 'Akhil Sharma', 'akhil@gmail.com', '$2a$10$A0guMTcOLrkOjwFMuWdFN.8UJOsnAaPBzZ0/3r6aLMDp37NxkQwAO', '22A01', 'AIML', 2, '9876543210', 1, 1, 1, 1, 'ACTIVE'),
(2, 'Sneha Reddy', 'sneha@gmail.com', '$2a$10$A0guMTcOLrkOjwFMuWdFN.8UJOsnAaPBzZ0/3r6aLMDp37NxkQwAO', '22A14', 'AIML', 3, '9876543211', 1, 2, 3, 3, 'ACTIVE'),
(3, 'Rahul Verma', 'rahul@gmail.com', '$2a$10$A0guMTcOLrkOjwFMuWdFN.8UJOsnAaPBzZ0/3r6aLMDp37NxkQwAO', '22C05', 'CSE',  2, '9876543212', 2, 4, 5, 1, 'ACTIVE'),
(4, 'Pooja Nair',  'pooja@gmail.com', '$2a$10$A0guMTcOLrkOjwFMuWdFN.8UJOsnAaPBzZ0/3r6aLMDp37NxkQwAO', '21A09', 'AIML', 4, '9876543213', 1, 3, 4, 3, 'ACTIVE'),
(5, 'Karan Joshi', 'karan@gmail.com', '$2a$10$A0guMTcOLrkOjwFMuWdFN.8UJOsnAaPBzZ0/3r6aLMDp37NxkQwAO', '22E32', 'ECE',  2, '9876543214', 3, 6, 6, 1, 'ACTIVE');

-- -----------------------------------------------------
-- 9. Registration Windows
-- -----------------------------------------------------
TRUNCATE TABLE registration_window;
INSERT INTO registration_window (id, title, semester_id, start_date_time, end_date_time, status, active) VALUES
(1, 'Semester 3 Course Registration Window 2025-26', 1, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 21 DAY), 'OPEN', TRUE),
(2, 'Semester 5 Course Registration Window 2025-26', 3, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 23 DAY), 'OPEN', TRUE);

-- -----------------------------------------------------
-- 10. Sample Registrations
-- -----------------------------------------------------
TRUNCATE TABLE registration;
INSERT INTO registration (registration_id, student_id, course_id, semester_id, registration_date, status) VALUES
(1, 1, 1, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 'REGISTERED'),
(2, 2, 4, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), 'REGISTERED'),
(3, 3, 7, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 'REGISTERED');

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
